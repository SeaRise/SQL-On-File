package com.searise.sof.schedule.stage;

import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.ResultHandle;

import java.util.List;

public class ResultStage extends Stage {
    public final ResultHandle resultHandle;
    public ResultStage(long stageId, List<Long> parentStageIds, PhysicalPlan plan, ResultHandle resultHandle) {
        super(stageId, parentStageIds, plan);
        this.resultHandle = resultHandle;
    }
}
