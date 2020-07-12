package com.searise.sof.plan.physics;

import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.plan.QueryPlan;

import java.util.List;

public interface PhysicalPlan extends QueryPlan {
    List<BoundReference> schema();

    void resolveIndex();
}
