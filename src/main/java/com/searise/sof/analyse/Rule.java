package com.searise.sof.analyse;

import com.searise.sof.plan.logic.LogicalPlan;

public interface Rule {
    LogicalPlan apply(LogicalPlan plan);

    default String ruleName() {
        return this.getClass().getSimpleName();
    }
}
