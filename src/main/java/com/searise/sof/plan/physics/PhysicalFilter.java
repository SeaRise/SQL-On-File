package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalFilter implements PhysicalPlan {
    public final List<BoundReference> schema;
    public List<Expression> conditions;
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
    public void resolveIndex() {
        child.resolveIndex();
        List<BoundReference> childSchema = child.schema();
        Map<Long, Integer> inputs = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        this.conditions = ReferenceResolver.resolveExpression(conditions, inputs);
        ReferenceResolver.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalFilter [%s]", conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
