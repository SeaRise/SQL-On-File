package com.searise.sof.schedule.dag;

import com.searise.sof.core.Utils;
import com.searise.sof.schedule.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StageStateMachine {
    private Map<Long, Stage> stageIdToStage = new HashMap<>();
    private Set<Long> waitingStageIds = new HashSet<>();
    private Set<Long> runningStageIds = new HashSet<>();
    private Set<Long> completeStageIds = new HashSet<>();

    public void addStage(Stage stage) {
        Utils.checkArgument(!stageIdToStage.containsKey(stage.stageId),
                String.format("stage(%s) has been added", stage.stageId));
        stageIdToStage.put(stage.stageId, stage);
    }

    public void stateToWaiting(long stageId) {
        stage(stageId);
        Utils.checkArgument(!runningStageIds.contains(stageId) && !completeStageIds.contains(stageId),
                "stage is running or completed, can not to run");
        waitingStageIds.add(stageId);
    }

    public void stateToRunning(long stageId) {
        stage(stageId);
        Utils.checkArgument(!completeStageIds.contains(stageId),
                "stage is completed, can not to run");
        waitingStageIds.remove(stageId);
        runningStageIds.add(stageId);
    }

    public void stateToComplete(long stageId) {
        stage(stageId);
        waitingStageIds.remove(stageId);
        runningStageIds.remove(stageId);
        completeStageIds.add(stageId);
    }

    public boolean isWaiting(long stageId) {
        return waitingStageIds.contains(stageId);
    }

    public boolean isRunning(long stageId) {
        return runningStageIds.contains(stageId);
    }

    public boolean isCompleted(long stageId) {
        return completeStageIds.contains(stageId);
    }

    public Stage stage(long stageId) {
        return Utils.checkNotNull(stageIdToStage.get(stageId),
                "stageIdToStage don't contains " + stageId);
    }

    public void clear() {
        stageIdToStage.clear();
        waitingStageIds.clear();
        runningStageIds.clear();
        completeStageIds.clear();
    }
}
