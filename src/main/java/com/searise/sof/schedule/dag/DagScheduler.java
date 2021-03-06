package com.searise.sof.schedule.dag;

import com.google.common.collect.ImmutableSet;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.ExecBuilder;
import com.searise.sof.execution.Executor;
import com.searise.sof.expression.Expression;
import com.searise.sof.plan.physics.Exchange;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.stage.ResultStage;
import com.searise.sof.schedule.dag.stage.ShuffleMapStage;
import com.searise.sof.schedule.dag.stage.Stage;
import com.searise.sof.schedule.dag.stage.event.Event;
import com.searise.sof.schedule.dag.stage.event.StageSubmit;
import com.searise.sof.schedule.dag.stage.event.TaskStatusUpdate;
import com.searise.sof.schedule.task.Task;
import com.searise.sof.schedule.task.TaskResult;
import com.searise.sof.schedule.task.TaskScheduler;
import com.searise.sof.shuffle.MapOutputTracker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

// Thread unsafe.
// 一次只能跑一个plan.
public class DagScheduler {
    public final TaskScheduler taskScheduler;
    private final AtomicLong nextStageId = new AtomicLong(0L);
    private final StageStateMachine stageStateMachine = new StageStateMachine();
    private final ExecBuilder builder;

    private final MapOutputTracker mapOutputTracker;

    // 用于阻塞主线程,直到plan执行完成.
    private volatile CountDownLatch planLatch;
    private volatile ResultStage finalStage;

    // 专门处理event的单线程.
    private final ExecutorService loopThread;
    private volatile boolean isRunning = true;
    private final BlockingDeque<Event> pendingQueue = new LinkedBlockingDeque<>();

    private volatile PlanExecResult planExecResult = null;

    public DagScheduler(SofContext context) {
        this.taskScheduler = new TaskScheduler(context, this);
        builder = new ExecBuilder(context);
        mapOutputTracker = context.mapOutputTracker;

        loopThread = Executors.newSingleThreadExecutor();
        loopThread.submit(() -> {
            while (isRunning) {
                try {
                    Event event = Utils.checkNotNull(pendingQueue.take(), "taskResult is null");
                    if (event instanceof TaskStatusUpdate) {
                        TaskStatusUpdate taskStatusUpdate = (TaskStatusUpdate) event;
                        doStatusUpdate(taskStatusUpdate.taskResult);
                    } else if (event instanceof StageSubmit) {
                        StageSubmit stageSubmit = (StageSubmit) event;
                        submitStageInternal(stageSubmit.stage);
                    } else {
                        throw new SofException("unknown event: " + event.getClass().getSimpleName());
                    }
                } catch (Throwable e) {
                    planFail(e);
                }
            }
        });
    }

    private void planSuccess() {
        clear();
        planExecResult = PlanExecResult.success();
        planLatch.countDown();
    }

    private void planFail(Throwable e) {
        clear();
        planExecResult = PlanExecResult.fail(e);
        planLatch.countDown();
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) {
        if (plan.partitions() <= 0) {
            return;
        }

        try {
            planLatch = new CountDownLatch(1);
            finalStage = createFinalStage(plan, resultHandle);
            pendingQueue.add(new StageSubmit(finalStage));
            planLatch.await();

            Utils.checkNotNull(planExecResult, "planExecResult is null").execResult();

            resetVolatile();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void statusUpdate(TaskResult taskResult) {
        pendingQueue.add(new TaskStatusUpdate(taskResult));
    }

    private void doStatusUpdate(TaskResult taskResult) {
        switch (taskResult.taskStatus) {
            case SUCCESS:
                handleTaskSuccess(taskResult.stageId, taskResult.partition);
                break;
            case FAIL:
                handleTaskFail(taskResult.stageId, taskResult.partition, taskResult.throwable);
                break;
            default:
                throw new SofException(String.format("unknown status(%s) for task(%s, %s)",
                        taskResult.taskStatus, taskResult.stageId, taskResult.partition));
        }
    }

    private void handleTaskSuccess(long stageId, int partition) {
        stageStateMachine.stage(stageId).success(partition);

        if (finalStage.getMissPartitions().isEmpty()) {
            planSuccess();
            return;
        }

        Stage stage = stageStateMachine.stage(stageId);
        if (stage.getMissPartitions().isEmpty()) {
            // unregister all parent shuffle output
            for (Long parentStageId : stage.parentStageIds) {
                Stage parent = stageStateMachine.stage(parentStageId);
                if (parent instanceof ShuffleMapStage) {
                    mapOutputTracker.unregisterShuffle(((ShuffleMapStage) parent).shuffleId);
                }
            }
            stageStateMachine.stateToComplete(stageId);
            // doNext
            submitNext(stage);
        }
    }

    private void handleTaskFail(long stageId, int partition, Throwable throwable) {
        Exception e = new SofException(
                String.format("plan fail because task(stageId: %s, partition: %s) fail",
                        stageId, partition), throwable);
        planFail(e);
    }

    private void submitMissingTasks(Stage stage) {
        List<Integer> partitionsToCompute = stage.getMissPartitions();
        if (partitionsToCompute.isEmpty()) {
            stageStateMachine.stateToComplete(stage.stageId);
            submitNext(stage);
            return;
        }

        Executor executor = builder.build(stage.plan);
        List<Task> tasks = Utils.toImmutableList(partitionsToCompute.stream().
                map(partition -> stage.buildTask(executor, partition)));
        taskScheduler.submit(tasks);
    }

    private void submitNext(Stage parentStage) {
        List<Stage> nextStages = stageStateMachine.getByParentStageId(parentStage.stageId);
        for (Stage nextStage : nextStages) {
            submitStage(nextStage);
        }
    }

    private void submitStage(Stage stage) {
        pendingQueue.add(new StageSubmit(stage));
    }

    private void submitStageInternal(Stage stage) {
        Utils.checkNotNull(stage, "submit null stage");

        if (stageStateMachine.isRunning(stage.stageId) ||
                stageStateMachine.isCompleted(stage.stageId)) {
            return;
        }

        boolean canRun = true;
        for (Long parentStageId : stage.parentStageIds) {
            if (!stageStateMachine.isCompleted(parentStageId)) {
                canRun = false;
            }
            submitStage(stageStateMachine.stage(parentStageId));
        }

        if (canRun) {
            stageStateMachine.stateToRunning(stage.stageId);
            submitMissingTasks(stage);
        } else {
            stageStateMachine.stateToWaiting(stage.stageId);
        }
    }

    private ResultStage createFinalStage(PhysicalPlan plan, ResultHandle resultHandle) {
        Set<Long> parentStageIds = createParentStages(plan);
        ResultStage finalStage = new ResultStage(nextStageId(), parentStageIds, plan, resultHandle);
        stageStateMachine.addStage(finalStage);
        return finalStage;
    }

    private Stage createShuffleMapStage(PhysicalPlan plan, long shuffleId, List<Expression> shuffleKeys, int reduceNum) {
        Set<Long> parentStageIds = createParentStages(plan);
        ShuffleMapStage shuffleMapStage = new ShuffleMapStage(nextStageId(), parentStageIds, plan, shuffleId, mapOutputTracker, shuffleKeys, reduceNum);
        stageStateMachine.addStage(shuffleMapStage);
        mapOutputTracker.registerShuffle(shuffleId, shuffleMapStage.partitions, reduceNum);
        return shuffleMapStage;
    }

    private Set<Long> createParentStages(PhysicalPlan plan) {
        ImmutableSet.Builder<Long> parentStageIdBuilder = ImmutableSet.builder();
        for (PhysicalPlan child : plan.children()) {
            if (!child.children().isEmpty()) {
                parentStageIdBuilder.addAll(createParentStages(child));
            } else if (child.children().isEmpty() && child instanceof Exchange) {
                Exchange exchange = (Exchange) child;
                Stage shuffleMapStage = createShuffleMapStage(exchange.mapPlan, exchange.shuffleId, exchange.keys, exchange.partitions());
                parentStageIdBuilder.add(shuffleMapStage.stageId);
            } else {
                // just else
            }
        }
        return parentStageIdBuilder.build();
    }

    private long nextStageId() {
        return nextStageId.getAndIncrement();
    }

    public void stop() {
        isRunning = false;
        loopThread.shutdownNow();
        taskScheduler.stop();
        resetVolatile();
    }

    // thread unsafe
    private void clear() {
        taskScheduler.clear();
        pendingQueue.clear();
        stageStateMachine.clear();
        mapOutputTracker.clear();
    }

    private void resetVolatile() {
        planLatch = null;
        finalStage = null;
        planExecResult = null;
        nextStageId.set(0L);
    }
}
