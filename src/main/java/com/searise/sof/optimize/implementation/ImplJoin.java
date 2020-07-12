package com.searise.sof.optimize.implementation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.physics.PhysicalNestedLoopJoin;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;

public class ImplJoin implements ImplementationRule {
    @Override
    public PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children) {
        Preconditions.checkArgument(children.size() == 2);
        InnerJoin join = (InnerJoin) groupExpr.exprNode;
        List<BoundReference> schema = Utils.toImmutableList(join.schema().stream().
                map(attr -> new BoundReference(attr.dataType, attr.exprId)));
        return new PhysicalNestedLoopJoin(schema, join.conditions, children.get(0), children.get(1));
    }
}
