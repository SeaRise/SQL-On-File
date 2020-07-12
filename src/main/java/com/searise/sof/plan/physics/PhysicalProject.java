package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.stream.Collectors;

public class PhysicalProject implements PhysicalPlan {
    public final List<BoundReference> schema;
    public final List<Expression> projectList;
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
    public void resolveSchema() {
        child.resolveSchema();
        List<BoundReference> childSchema = child.schema();
        SchemaResolver.resolve(schema, Utils.toImmutableList(childSchema.stream().map(c -> c.exprId)));
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public String toString() {
        return String.format("PhysicalProject [%s]", projectList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
