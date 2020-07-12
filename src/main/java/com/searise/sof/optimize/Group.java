package com.searise.sof.optimize;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;
import java.util.Optional;

public class Group {
    public final List<Attribute> schema;
    private Optional<GroupExpr> groupExprOptional;

    public Group(List<Attribute> schema) {
        this.schema = schema;
        this.groupExprOptional = Optional.empty();
    }

    public void replace(GroupExpr groupExpr) {
        this.groupExprOptional = Optional.of(groupExpr);
    }

    public GroupExpr groupExpr() {
        Utils.checkArgument(groupExprOptional.isPresent(), "GroupExpr of Group is null");
        return groupExprOptional.get();
    }

    // Convert2GroupExpr converts a logical plan to a GroupExpr.
    public static GroupExpr convert2GroupExpr(LogicalPlan node, Group group) {
        List<Group> children = Utils.toImmutableList(node.children().stream().map(Group::convert2Group));
        return new GroupExpr(node, children, group);
    }

    // Convert2Group converts a logical plan to a Group.
    public static Group convert2Group(LogicalPlan node) {
        Group group = new Group(node.schema());
        GroupExpr groupExpr = convert2GroupExpr(node, group);
        group.replace(groupExpr);
        return group;
    }
}
