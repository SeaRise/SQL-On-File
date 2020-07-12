package com.searise.sof.plan.physics;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;

public class SchemaResolver {
    private SchemaResolver() {
    }

    public static void resolve(List<BoundReference> schema, List<Long> inputs) {
        for (BoundReference reference : schema) {
            boolean isResolved = false;
            for (int index = 0; index < inputs.size(); index++) {
                long input = inputs.get(index);
                if (reference.exprId == input) {
                    reference.resolveIndex(index);
                    isResolved = true;
                    break;
                }
            }
            Utils.checkArgument(isResolved, "can not resolve index for " + reference);
        }
    }
}
