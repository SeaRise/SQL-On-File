package com.searise.sof.stats;

import com.searise.sof.expression.Expression;
import com.searise.sof.expression.logic.Not;
import com.searise.sof.expression.logic.And;
import com.searise.sof.expression.logic.Or;

import java.util.List;

public class ConditionEstimation {
    private static final double singleConditionFactor = 0.8;

    static double estimateSelectivity(List<Expression> conditions) {
        double selectivity = 1;
        for (Expression condition : conditions) {
            selectivity *= estimate(condition);
        }
        return selectivity;
    }

    private static double estimate(Expression condition) {
        if (condition.getClass() == And.class) {
            And and = (And) condition;
            return estimate(and.left) * estimate(and.right);
        } else if (condition.getClass() == Or.class) {
            Or or = (Or) condition;
            double percent1 = estimate(or.left);
            double percent2 = estimate(or.right);
            return percent1 + percent2 - (percent1 * percent2);
        } else if (condition.getClass() == Not.class) {
            return 1 - estimate(condition.children().get(0));
        } else {
            // 默认单个条件选择率为singleConditionFactor
            return singleConditionFactor;
        }
    }
}
