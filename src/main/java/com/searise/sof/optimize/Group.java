package com.searise.sof.optimize;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.optimize.iter.Iterator;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.stats.Statistics;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

public class Group {
    public final List<Attribute> schema;
    private Iterator<GroupExpr> equivalents;
    public boolean explored = false;
    Optional<Pair<PhysicalPlan, Integer>> impl = Optional.empty();
    public Optional<Statistics> stats = Optional.empty();

    public Group(List<Attribute> schema) {
        this.schema = schema;
        this.equivalents = new com.searise.sof.optimize.iter.Iterator<>();
    }

    public void insert(GroupExpr groupExpr) {
        groupExpr.group = this;
        equivalents.add(groupExpr);
    }

    public Iterator<GroupExpr> iter() {
        return equivalents;
    }

    @Override
    public String toString() {
        Iterator<GroupExpr> iter = equivalents.newReadOnlyIter();
        StringBuilder builder = new StringBuilder().append("group:");
        while (iter.hasNext()) {
            builder.append(iter.next());
        }
        return builder.toString();
    }

    // Convert2GroupExpr converts a logical plan to a GroupExpr.
    public static GroupExpr convert2GroupExpr(LogicalPlan node, Group group) {
        List<Group> children = Utils.toImmutableList(node.children().stream().map(Group::convert2Group));
        return new GroupExpr(node, children);
    }

    // Convert2Group converts a logical plan to a Group.
    public static Group convert2Group(LogicalPlan node) {
        Group group = new Group(node.schema());
        GroupExpr groupExpr = convert2GroupExpr(node, group);
        group.insert(groupExpr);
        return group;
    }

    public static Group newGroup(LogicalPlan node, List<Group> children, List<Attribute> schema) {
        Group group = new Group(schema);
        GroupExpr groupExpr = new GroupExpr(node, children);
        group.insert(groupExpr);
        return group;
    }
}
