package com.searise.sof.optimize.implementation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.plan.logic.Relation;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.physics.PhysicalScan;

import java.util.List;

public class ImplRelation implements ImplementationRule {
    @Override
    public PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children) {
        Preconditions.checkArgument(children.isEmpty());
        Relation relation = (Relation) groupExpr.exprNode;
        List<BoundReference> schema = Utils.toImmutableList(relation.schema().stream().
                map(attr -> new BoundReference(attr.dataType, attr.exprId)));
        return new PhysicalScan(schema, relation.catalogTable.filePath, relation.catalogTable.separator);
    }
}
