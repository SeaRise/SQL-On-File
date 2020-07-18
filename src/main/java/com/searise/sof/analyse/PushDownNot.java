package com.searise.sof.analyse;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.logic.And;
import com.searise.sof.expression.logic.Not;
import com.searise.sof.expression.logic.Or;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public class PushDownNot implements Rule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.getClass() == Filter.class) {
                Filter filter = (Filter) p;
                List<Expression> afterPushDown = Utils.toImmutableList(filter.conditions.stream().map(this::pushDownNot));
                if (AnalysisHelper.isEqualTo(filter.conditions, afterPushDown)) {
                    return filter;
                }
                return new Filter(afterPushDown, filter.child, filter.context);
            } else if (p.getClass() == InnerJoin.class) {
                InnerJoin join = (InnerJoin) p;
                List<Expression> afterPushDown = Utils.toImmutableList(join.conditions.stream().map(this::pushDownNot));
                if (AnalysisHelper.isEqualTo(join.conditions, afterPushDown)) {
                    return join;
                }
                return new InnerJoin(join.left, join.right, afterPushDown, join.context);
            } else {
                return p;
            }
        });
    }

    private Expression pushDownNot(Expression expr) {
        return expr.transformDown((Applicable<Expression>) expression -> {
            if (expression.getClass() == Not.class) {
                Not not = (Not) expression;
                Expression child = not.child;
                if (child.getClass() == And.class) {
                    And and = (And) child;
                    return new Or(new Not(and.left), new Not(and.right));
                } else if (child.getClass() == Or.class) {
                    Or or = (Or) child;
                    return new And(new Not(or.left), new Not(or.right));
                } else {
                    return expression;
                }
            }
            return expression;
        });
    }
}
