package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.ExprIdGetter;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalProject implements PhysicalPlan {
    public List<BoundReference> schema;
    public List<Expression> projectList;
    public final PhysicalPlan child;

    public PhysicalProject(List<BoundReference> schema, List<Expression> projectList, PhysicalPlan child) {
        this.schema = schema;
        this.projectList = projectList;
        this.child = child;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public void resolveIndex() {
        child.resolveIndex();
        List<BoundReference> childSchema = child.schema();
        this.projectList = ReferenceResolver.resolveExpression(projectList, Utils.zip(index -> childSchema.get(index).exprId, childSchema.size()));
        for (int i = 0; i < this.schema.size(); i++) {
            schema.get(i).resolveIndex(i);
        }
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("PhysicalProject [%s] [%s]", schemaToString(), projectList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        if (!isTop) {
            Map<Long, Integer> exprIds = Utils.zip(index -> schema.get(index).exprId, schema.size());
            ImmutableList.Builder<Expression> newProjectListBuilder = ImmutableList.builder();
            for (BoundReference reference : father) {
                int index = exprIds.getOrDefault(reference.exprId, -1);
                if (index >= 0) {
                    newProjectListBuilder.add(projectList.get(index));
                }
            }
            projectList = newProjectListBuilder.build();
            schema = Utils.copy(father);
        }

        child.prune(extractUseSchema(projectList), false);
    }
}
