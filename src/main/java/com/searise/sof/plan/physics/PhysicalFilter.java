package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.stream.Collectors;

public class PhysicalFilter implements PhysicalPlan {
    public final List<BoundReference> schema;
    public final List<Expression> conditions;
    public final PhysicalPlan child;

    public PhysicalFilter(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan child) {
        this.schema = schema;
        this.conditions = conditions;
        this.child = child;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public void resolveSchema() {
        child.resolveSchema();
        List<BoundReference> childSchema = child.schema();
        SchemaResolver.resolve(schema, Utils.toImmutableList(childSchema.stream().map(c -> c.exprId)));
    }

    @Override
    public String toString() {
        return String.format("PhysicalFilter [%s]", conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
