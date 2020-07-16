package com.searise.sof.plan.physics;

import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.RemoveAliasHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import com.searise.sof.plan.QueryPlan;

import java.util.List;
import java.util.stream.Collectors;

public interface PhysicalPlan extends QueryPlan, ReferenceResolveHelper, SchemaPruneHelper, RemoveAliasHelper {
    List<BoundReference> schema();

    default String schemaToString() {
        return schema().stream().map(BoundReference::toString).collect(Collectors.joining(","));
    }
}
