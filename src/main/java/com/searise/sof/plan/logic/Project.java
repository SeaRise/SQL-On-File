package com.searise.sof.plan.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Project implements LogicalPlan {
    public final LogicalPlan child;
    public final List<Expression> projectList;
    public Project(List<Expression> projectList, LogicalPlan child) {
        this.child = child;
        this.projectList = projectList;
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("Project [%s]", projectList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public LogicalPlan copyWithNewChildren(List<LogicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new Project(projectList, children.get(0));
    }

    @Override
    public boolean resolved() {
        return child.resolved() && projectList.stream().allMatch(AnalysisHelper::resolved);
    }
}
