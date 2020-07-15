package com.searise.sof.plan.physics;

import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.plan.QueryPlan;

import java.util.List;
import java.util.stream.Collectors;

public interface PhysicalPlan extends QueryPlan, SchemaPruneHelper {
    List<BoundReference> schema();

    void resolveIndex();

    default String schemaToString() {
        return schema().stream().map(BoundReference::toString).collect(Collectors.joining(","));
    }
}
