package com.searise.sof.optimize;

import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

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
}
