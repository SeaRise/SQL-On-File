package com.searise.sof.schedule.dag;

import com.google.common.collect.ImmutableSet;
import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Builder;
import com.searise.sof.execution.Executor;
import com.searise.sof.expression.Expression;
import com.searise.sof.plan.physics.Exchange;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.stage.event.Event;
import com.searise.sof.schedule.dag.stage.event.StageSubmit;
import com.searise.sof.schedule.dag.stage.event.TaskStatusUpdate;
import com.searise.sof.schedule.task.Task;
import com.searise.sof.schedule.dag.stage.ResultStage;
import com.searise.sof.schedule.dag.stage.ShuffleMapStage;
import com.searise.sof.schedule.dag.stage.Stage;
import com.searise.sof.schedule.task.TaskResult;
import com.searise.sof.schedule.task.TaskScheduler;
import com.searise.sof.shuffle.MapOutputTracker;

import java.util.*;
import java.util.concurrent.*;

// Thread unsafe.
// 一次只能跑一个plan.
public class DagScheduler {
    private final TaskScheduler taskScheduler;
    private long nextStageId = 0;
    private StageStateMachine stageStateMachine = new StageStateMachine();
    private final Builder builder;

    private final MapOutputTracker mapOutputTracker;

    private CountDownLatch planLatch;
    private ResultStage finalStage;

    private final ExecutorService loopThread;
    private volatile boolean isRunning = true;
    private final BlockingDeque<Event> pendingQueue = new LinkedBlockingDeque<>();

    public DagScheduler(Context context) {
        this.taskScheduler = new TaskScheduler(context, this);
        builder = new Builder(context);
        mapOutputTracker = new MapOutputTracker(context);

        loopThread = Executors.newSingleThreadExecutor();
        loopThread.submit(() -> {
            while (isRunning) {
                Event event;
                try {
                    event = Utils.checkNotNull(pendingQueue.take(), "taskResult is null");
                } catch (Exception e) {
                    continue;
                }
                if (event instanceof TaskStatusUpdate) {
                    TaskStatusUpdate taskStatusUpdate = (TaskStatusUpdate) event;
                    doStatusUpdate(taskStatusUpdate.taskResult);
                } else if (event instanceof StageSubmit) {
                    StageSubmit stageSubmit = (StageSubmit) event;
                    submitStage(stageSubmit.stage);
                } else {
                    throw new SofException("unknown event: " + event.getClass().getSimpleName());
                }
            }
        });
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) throws InterruptedException {
        planLatch = new CountDownLatch(1);
        finalStage = createFinalStage(plan, resultHandle);
        pendingQueue.add(new StageSubmit(finalStage));
        planLatch.await();
        reset();
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
        if (stageId == finalStage.stageId) {
            finalStage.success(partition);
        }

        if (finalStage.isSuccess()) {
            // plan success
            planLatch.countDown();
        }

        // doNext
        submitNext(stageStateMachine.stage(stageId));
    }

    private void handleTaskFail(long stageId, int partition, Throwable throwable) {

    }

    private void submitMissingTasks(Stage stage) {
        List<Integer> partitionsToCompute = stage.getMissPartitions();
        if (partitionsToCompute.isEmpty()) {
            stageStateMachine.stateToComplete(stage.stageId);
            submitNext(stage);
        }

        Executor executor =  builder.build(stage.plan);
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
        long stageId = nextStageId();
        ResultStage finalStage = new ResultStage(stageId, parentStageIds, plan, resultHandle);
        stageStateMachine.addStage(finalStage);
        return finalStage;
    }

    private Stage createShuffleMapStage(PhysicalPlan plan, long shuffleId, List<Expression> shuffleKeys, int reduceNum) {
        Set<Long> parentStageIds = createParentStages(plan);
        long stageId = nextStageId();
        ShuffleMapStage shuffleMapStage = new ShuffleMapStage(stageId, parentStageIds, plan, shuffleId, mapOutputTracker, shuffleKeys, reduceNum);
        stageStateMachine.addStage(shuffleMapStage);
        mapOutputTracker.registerShuffle(shuffleId, shuffleMapStage.partitions);
        return shuffleMapStage;
    }

    private Set<Long> createParentStages(PhysicalPlan plan) {
        int partitions = plan.partitions();
        ImmutableSet.Builder<Long> parentStageIdBuilder = ImmutableSet.builder();
        for (PhysicalPlan child : plan.children()) {
            if (!child.children().isEmpty()) {
                parentStageIdBuilder.addAll(createParentStages(child));
            } else if (child.children().isEmpty() && child instanceof Exchange) {
                Exchange exchange = (Exchange) child;
                Stage parentStage = createShuffleMapStage(exchange.mapPlan, exchange.shuffleId, exchange.keys, partitions);
                parentStageIdBuilder.add(parentStage.stageId);
            } else {
                // just else
            }
        }
        return parentStageIdBuilder.build();
    }

    private long nextStageId() {
        return nextStageId++;
    }

    public void stop() {
        isRunning = false;
        loopThread.shutdownNow();
        taskScheduler.stop();
        reset();
    }

    private void reset() {
        stageStateMachine.clear();
        planLatch = null;
        finalStage = null;
    }
}
