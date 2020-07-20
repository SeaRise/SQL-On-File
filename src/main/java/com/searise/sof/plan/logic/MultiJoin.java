package com.searise.sof.plan.logic;

import com.searise.sof.core.Context;
import com.searise.sof.expression.Expression;

import java.util.List;

public class MultiJoin implements LogicalPlan {
    public final List<Expression> conditions;
    public final Context context;

    public MultiJoin(List<Expression> conditions, Context context) {
        this.conditions = conditions;
        this.context = context;
    }

    @Override
    public Context context() {
        return context;
    }
}
