package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.stream.Collectors;

public class PhysicalNestedLoopJoin implements PhysicalPlan {
    public final List<BoundReference> schema;
    public final List<Expression> conditions;
    public final PhysicalPlan stream;
    public final PhysicalPlan build;

    public PhysicalNestedLoopJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build) {
        this.schema = schema;
        this.conditions = conditions;
        this.stream = stream;
        this.build = build;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(stream, build);
    }

    @Override
    public void resolveSchema() {
        stream.resolveSchema();
        build.resolveSchema();
        List<BoundReference> childSchema = Utils.combine(stream.schema(), build.schema());
        SchemaResolver.resolve(schema, Utils.toImmutableList(childSchema.stream().map(c -> c.exprId)));
    }

    @Override
    public String toString() {
        return String.format("PhysicalNestedLoopJoin [%s]", conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}