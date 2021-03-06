package com.searise.sof.plan.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.core.SofContext;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Filter implements LogicalPlan {
    public final LogicalPlan child;
    public final List<Expression> conditions;
    public final SofContext context;

    public Filter(List<Expression> conditions, LogicalPlan child, SofContext context) {
        this.child = child;
        this.conditions = conditions;
        this.context = context;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public SofContext context() {
        return context;
    }

    @Override
    public List<Attribute> schema() {
        return child.schema();
    }

    @Override
    public String toString() {
        return String.format("Filter [%s]", conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public LogicalPlan copyWithNewChildren(List<LogicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new Filter(conditions, children.get(0), context);
    }

    @Override
    public boolean resolved() {
        return child.resolved() && conditions.stream().allMatch(AnalysisHelper::resolved);
    }
}
