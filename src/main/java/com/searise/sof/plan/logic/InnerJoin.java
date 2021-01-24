package com.searise.sof.plan.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InnerJoin implements LogicalPlan {
    public final LogicalPlan left;
    public final LogicalPlan right;
    public final List<Expression> conditions;
    public final SofContext context;

    public InnerJoin(LogicalPlan left, LogicalPlan right, SofContext context) {
        this(left, right, ImmutableList.of(), context);
    }

    public InnerJoin(LogicalPlan left, LogicalPlan right, List<Expression> conditions, SofContext context) {
        this.left = left;
        this.right = right;
        this.conditions = conditions;
        this.context = context;
    }

    @Override
    public SofContext context() {
        return context;
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

    @Override
    public LogicalPlan copyWithNewChildren(List<LogicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new InnerJoin(children.get(0), children.get(1), conditions, context);
    }

    @Override
    public boolean resolved() {
        return left.resolved() && right.resolved() && conditions.stream().allMatch(AnalysisHelper::resolved);
    }

    @Override
    public List<Attribute> schema() {
        return Utils.combineDistinct(left.schema(), right.schema());
    }
}
