package com.searise.sof.schedule.dag;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Builder;
import com.searise.sof.plan.physics.Exchange;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.stage.ResultStage;
import com.searise.sof.schedule.stage.ShuffleMapStage;
import com.searise.sof.schedule.stage.Stage;
import com.searise.sof.schedule.task.TaskScheduler;

import java.util.*;

// Thread unsafe.
// 一次只能跑一个plan.
public class DagScheduler {
    private final TaskScheduler taskScheduler;
    private long nextStageId = 0;
    private StageStateMachine stageStateMachine = new StageStateMachine();
    private final Builder builder;

    public DagScheduler(Context context) {
        this.taskScheduler = new TaskScheduler(context, this);
        builder = new Builder(context);
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) {
        Stage finalStage = createFinalStage(plan, resultHandle);
        submitStage(finalStage);
    }

    private void submitMissingTasks(Stage stage) {
        List<Integer> partitionsToCompute = ImmutableList.of();
        if (partitionsToCompute.isEmpty()) {
            stageStateMachine.stateToComplete(stage.stageId);
            submitNext(stage);
        }

        for (Integer partition : partitionsToCompute) {
            builder.build(stage.plan);
        }
    }

    private void submitNext(Stage parentStage) {

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

    private Stage createFinalStage(PhysicalPlan plan, ResultHandle resultHandle) {
        List<Long> parentStageIds = createParentStages(plan);
        long stageId = nextStageId();
        ResultStage finalStage = new ResultStage(stageId, parentStageIds, plan, resultHandle);
        stageStateMachine.addStage(finalStage);
        return finalStage;
    }

    private Stage createShuffleMapStage(PhysicalPlan plan, long shuffleId) {
        List<Long> parentStageIds = createParentStages(plan);
        long stageId = nextStageId();
        ShuffleMapStage shuffleMapStage = new ShuffleMapStage(stageId, parentStageIds, plan, shuffleId);
        stageStateMachine.addStage(shuffleMapStage);
        return shuffleMapStage;
    }

    private List<Long> createParentStages(PhysicalPlan plan) {
        ImmutableList.Builder<Long> parentStageIdBuilder = ImmutableList.builder();
        for (PhysicalPlan child : plan.children()) {
            if (!child.children().isEmpty()) {
                parentStageIdBuilder.addAll(createParentStages(child));
            } else if (child.children().isEmpty() && child instanceof Exchange) {
                Exchange exchange = (Exchange) child;
                Stage parentStage = createShuffleMapStage(exchange.mapPlan, exchange.shuffleId);
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

        reset();
    }

    private void reset() {
        taskScheduler.stop();
    }
}
