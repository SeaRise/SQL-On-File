package com.searise.sof.plan.physics;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.RemoveAliasHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import com.searise.sof.plan.QueryPlan;

import java.util.List;
import java.util.stream.Collectors;

public interface PhysicalPlan extends QueryPlan<PhysicalPlan>, ReferenceResolveHelper, SchemaPruneHelper, RemoveAliasHelper {
    List<BoundReference> schema();

    default String schemaToString() {
        return schema().stream().map(BoundReference::toString).collect(Collectors.joining(","));
    }

    default void removeAlias() {
        for (PhysicalPlan child : children()) {
            child.removeAlias();
        }
    }

    default int partitions() {
        List<PhysicalPlan> children = children();
        if (children.isEmpty()) {
            return 0;
        }

        int partitions = children.get(0).partitions();
        for (int i = 1; i < children.size(); i++) {
            int childPartitions = children.get(i).partitions();
            Utils.checkArgument(partitions == childPartitions,
                    String.format("children(0).partitions(%s) != children(%s).partitions(%s)",
                            partitions, i, childPartitions));
        }
        return partitions;
    }
}
