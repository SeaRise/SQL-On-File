package com.searise.sof.schedule.stage;

import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;

public class ShuffleMapStage extends Stage {
    public final long shuffleId;
    public ShuffleMapStage(long stageId, List<Long> parentStageIds, PhysicalPlan plan, long shuffleId) {
        super(stageId, parentStageIds, plan);
        this.shuffleId = shuffleId;
    }
}
