package com.searise.sof.plan.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.common.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Project implements LogicalPlan {
    public final LogicalPlan child;
    public final List<Expression> projectList;
    private List<Attribute> schema = ImmutableList.of();
    public Project(List<Expression> projectList, LogicalPlan child) {
        this.child = child;
        this.projectList = projectList;
        refreshSchema();
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(child);
    }

    private void refreshSchema() {
        if (!resolved()) {
            return;
        }

        this.schema = Utils.toImmutableList(projectList.stream().map(expr -> {
            if (expr.getClass() == Attribute.class) {
                return (Attribute) expr;
            } else if (expr.getClass() == Alias.class) {
                Alias alias = (Alias) expr;
                return (Attribute) alias.attribute;
            } else {
                return Attribute.newUnknownAttribute(expr.dataType());
            }
        }));
    }

    @Override
    public List<Attribute> schema() {
       return schema;
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
