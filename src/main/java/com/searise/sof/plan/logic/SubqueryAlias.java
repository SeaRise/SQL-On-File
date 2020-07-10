package com.searise.sof.plan.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class SubqueryAlias implements LogicalPlan {
    public final String name;
    public final LogicalPlan child;
    public SubqueryAlias(String name, LogicalPlan child) {
        this.name = name;
        this.child = child;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("SubqueryAlias [%s]", name);
    }

    @Override
    public LogicalPlan copyWithNewChildren(List<LogicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new SubqueryAlias(name, children.get(0));
    }

    @Override
    public boolean resolved() {
        return child.resolved();
    }
}
