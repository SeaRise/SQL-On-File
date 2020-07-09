package com.searise.sof.plan.logic;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.stream.Collectors;

public class Filter implements LogicalPlan {
    public final LogicalPlan child;
    public final List<Expression> conditions;
    public Filter(LogicalPlan child, List<Expression> conditions) {
        this.child = child;
        this.conditions = conditions;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("Filter [%s]", conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
