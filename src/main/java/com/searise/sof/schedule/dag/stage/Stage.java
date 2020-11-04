package com.searise.sof.schedule.dag.stage;

import com.searise.sof.execution.Executor;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.task.Task;

import java.util.List;
import java.util.Set;

public abstract class Stage {
    public final long stageId;
    // 上游游stage的stageId
    public final Set<Long> parentStageIds;
    public final PhysicalPlan plan;
    public final int partitions;

    public Stage(long stageId, Set<Long> parentStageIds, PhysicalPlan plan) {
        this.stageId = stageId;
        this.parentStageIds = parentStageIds;
        this.plan = plan;
        this.partitions = plan.partitions();
    }

    public abstract Task buildTask(Executor executor, int partition);

    public abstract List<Integer> getMissPartitions();
}