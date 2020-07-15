package com.searise.sof.optimize.implementation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.physics.PhysicalFilter;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;

public class ImplFilter implements ImplementationRule {
    @Override
    public PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children) {
        Preconditions.checkArgument(children.size() == 1);
        Filter filter = (Filter) groupExpr.exprNode;
        List<BoundReference> schema = Utils.toImmutableList(groupExpr.group.schema.stream().
                map(attr -> new BoundReference(attr.dataType, attr.exprId)));
        return new PhysicalFilter(schema, filter.conditions, children.get(0));
    }
}
