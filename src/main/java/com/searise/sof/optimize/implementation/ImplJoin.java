package com.searise.sof.optimize.implementation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.conf.Conf;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.physics.PhysicalHashJoin;
import com.searise.sof.plan.physics.PhysicalNestedLoopJoin;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.stats.SizeInBytesStatsVisitor;
import com.searise.sof.stats.Statistics;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.List;

public class ImplJoin implements ImplementationRule {
    @Override
    public PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children) {
        Preconditions.checkArgument(children.size() == 2);

        List<BoundReference> schema = Utils.toImmutableList(groupExpr.group.schema.stream().
                map(attr -> new BoundReference(attr.dataType, attr.exprId)));
        return selectJoin(groupExpr, schema, children);
    }

    private PhysicalPlan selectJoin(GroupExpr groupExpr, List<BoundReference> schema, List<PhysicalPlan> children) {
        InnerJoin join = (InnerJoin) groupExpr.exprNode;
        String forceJoinType = join.context.conf.getConf(Conf.FORCE_JOIN_TYPE);

        if (StringUtils.equals(forceJoinType, Conf.FORCE_JOIN_TYPE_LOOP_VALUE)) {
            return selectLoopJoin(groupExpr, join, schema, children);
        } else if (StringUtils.equals(forceJoinType, Conf.FORCE_JOIN_TYPE_HASH_VALUE)) {
            return selectHashJoin(groupExpr, join, schema, children);
        } else {
            BigInteger threshold = BigInteger.valueOf(join.context.conf.getConf(Conf.AUTO_HASH_JOIN_THRESHOLD));

            Statistics leftStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(0));
            Statistics rightStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(1));
            if (leftStats.sizeInBytes.compareTo(threshold) <= 0 || rightStats.sizeInBytes.compareTo(threshold) <= 0) {
                return selectHashJoin(groupExpr, join, schema, children);
            }

            return selectLoopJoin(groupExpr, join, schema, children);
        }
    }

    private PhysicalNestedLoopJoin selectLoopJoin(GroupExpr groupExpr, InnerJoin join, List<BoundReference> schema, List<PhysicalPlan> children) {
        Statistics leftStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(0));
        Statistics rightStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(1));
        if (leftStats.sizeInBytes.compareTo(rightStats.sizeInBytes) >= 0) {
            return new PhysicalNestedLoopJoin(schema, join.conditions, children.get(0), children.get(1), join.context);
        }
        return new PhysicalNestedLoopJoin(schema, join.conditions, children.get(1), children.get(0), join.context);
    }

    private PhysicalHashJoin selectHashJoin(GroupExpr groupExpr, InnerJoin join, List<BoundReference> schema, List<PhysicalPlan> children) {
        Statistics leftStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(0));
        Statistics rightStats = SizeInBytesStatsVisitor.visit(groupExpr.children.get(1));
        if (leftStats.sizeInBytes.compareTo(rightStats.sizeInBytes) >= 0) {
            return new PhysicalHashJoin(schema, join.conditions, children.get(0), children.get(1), join.context);
        }
        return new PhysicalHashJoin(schema, join.conditions, children.get(1), children.get(0), join.context);
    }
}
