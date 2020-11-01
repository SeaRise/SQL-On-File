package com.searise.sof.schedule.stage;

import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;

public abstract class Stage {
    public final long stageId;
    // 上游游stage的stageId
    public final List<Long> parentStageIds;
    public final PhysicalPlan plan;
    public final int partitions;

    public Stage(long stageId, List<Long> parentStageIds, PhysicalPlan plan) {
        this.stageId = stageId;
        this.parentStageIds = parentStageIds;
        this.plan = plan;
        this.partitions = plan.partitions();
    }
}