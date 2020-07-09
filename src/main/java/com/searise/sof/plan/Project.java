package com.searise.sof.plan;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.stream.Collectors;

public class Project implements LogicalPlan {
    public final LogicalPlan child;
    public final List<Expression> projectList;
    public Project(LogicalPlan child, List<Expression> projectList) {
        this.child = child;
        this.projectList = projectList;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("Project [%s]", projectList.stream().map(Object::toString).collect(Collectors.joining()));
    }
}
