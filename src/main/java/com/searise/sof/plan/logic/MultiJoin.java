package com.searise.sof.plan.logic;

import com.searise.sof.expression.Expression;

import java.util.List;

public class MultiJoin implements LogicalPlan {
    public final List<Expression> conditions;

    public MultiJoin(List<Expression> conditions) {
        this.conditions = conditions;
    }
}
