package com.searise.sof.optimize;

import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;
import java.util.stream.Collectors;

public class GroupExpr {
    public final LogicalPlan exprNode;
    public final List<Group> children;
    public final Group group;
    public boolean explored = false;

    public GroupExpr(LogicalPlan exprNode, List<Group> children, Group group) {
        this.exprNode = exprNode;
        this.children = children;
        this.group = group;
    }

    @Override
    public String toString() {
        return exprNode.toString() + "\n" + children.stream().map(Group::toString).collect(Collectors.joining());
    }
}
