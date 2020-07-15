package com.searise.sof.optimize.implementation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.plan.logic.Project;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.physics.PhysicalProject;

import java.util.List;

public class ImplProject implements ImplementationRule {
    @Override
    public PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children) {
        Preconditions.checkArgument(children.size() == 1);
        Project project = (Project) groupExpr.exprNode;
        List<BoundReference> schema = Utils.toImmutableList(groupExpr.group.schema.stream().
                map(attr -> new BoundReference(attr.dataType, attr.exprId)));
        return new PhysicalProject(schema, project.projectList, children.get(0));
    }
}
