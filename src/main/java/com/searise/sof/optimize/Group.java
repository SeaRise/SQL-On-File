package com.searise.sof.optimize;

import com.searise.sof.common.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Group {
    public final List<Attribute> schema;
    private final List<GroupExpr> equivalents;

    public Group(List<Attribute> schema) {
        this.equivalents = new ArrayList<>();
        this.schema = schema;
    }

    public void insert(GroupExpr groupExpr) {
        equivalents.add(groupExpr);
    }

    public Iterator<GroupExpr> iterator() {
        return equivalents.iterator();
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
        group.insert(groupExpr);
        return group;
    }
}
