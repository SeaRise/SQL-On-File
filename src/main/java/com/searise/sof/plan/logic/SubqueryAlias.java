package com.searise.sof.plan.logic;

import com.google.common.collect.ImmutableList;

import java.util.List;

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
}
