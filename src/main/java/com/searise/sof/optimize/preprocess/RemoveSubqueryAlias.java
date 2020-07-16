package com.searise.sof.optimize.preprocess;

import com.searise.sof.analyse.Applicable;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.SubqueryAlias;

public class RemoveSubqueryAlias implements PreprocessRule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformDown((Applicable<LogicalPlan>) p -> {
            if (p.getClass() == SubqueryAlias.class) {
                return p.children().get(0);
            }
            return p;
        });
    }
}