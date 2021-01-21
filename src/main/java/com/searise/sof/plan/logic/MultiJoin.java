package com.searise.sof.plan.logic;

import com.searise.sof.core.SofContext;
import com.searise.sof.expression.Expression;

import java.util.List;

public class MultiJoin implements LogicalPlan {
    public final List<Expression> conditions;
    public final SofContext context;

    public MultiJoin(List<Expression> conditions, SofContext context) {
        this.conditions = conditions;
        this.context = context;
    }

    @Override
    public SofContext context() {
        return context;
    }
}
