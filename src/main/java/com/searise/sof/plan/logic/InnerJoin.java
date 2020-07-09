package com.searise.sof.plan.logic;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.stream.Collectors;

public class InnerJoin implements LogicalPlan {
    public final LogicalPlan left;
    public final LogicalPlan right;
    public final List<Expression> conditions;

    public InnerJoin(LogicalPlan left, LogicalPlan right) {
        this(left, right, ImmutableList.of());
    }

    public InnerJoin(LogicalPlan left, LogicalPlan right, List<Expression> conditions) {
        this.left = left;
        this.right = right;
        this.conditions = conditions;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(left, right);
    }

    @Override
    public String toString() {
        String condSql = conditions.isEmpty() ? "" : String.format(" on (%s)",
                conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return "join" + condSql;
    }
}
