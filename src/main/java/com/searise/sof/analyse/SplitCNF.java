package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.logic.And;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public class SplitCNF implements Rule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.getClass() == Filter.class) {
                Filter filter = (Filter) p;
                List<Expression> afterSplit = Utils.toImmutableList(filter.conditions.stream().flatMap(c -> splitCNF(c).stream()));
                if (AnalysisHelper.isEqualTo(filter.conditions, afterSplit)) {
                    return filter;
                }
                return new Filter(afterSplit, filter.child, filter.context);
            } else if (p.getClass() == InnerJoin.class) {
                InnerJoin join = (InnerJoin) p;
                List<Expression> afterSplit = Utils.toImmutableList(join.conditions.stream().flatMap(c -> splitCNF(c).stream()));
                if (AnalysisHelper.isEqualTo(join.conditions, afterSplit)) {
                    return join;
                }
                return new InnerJoin(join.left, join.right, afterSplit, join.context);
            } else {
                return p;
            }
        });
    }

    private List<Expression> splitCNF(Expression cond) {
        if (cond.getClass() != And.class) {
            return ImmutableList.of(cond);
        }

        And and = (And) cond;
        return Utils.combineDistinct(splitCNF(and.left), splitCNF(and.right));
    }
}
