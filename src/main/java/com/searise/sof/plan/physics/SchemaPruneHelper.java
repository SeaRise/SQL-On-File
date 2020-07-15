package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;

public interface SchemaPruneHelper {
    void prune(List<BoundReference> father, boolean isTop);
    static List<BoundReference> copy(List<BoundReference> references) {
        return Utils.toImmutableList(references.stream().map(r -> new BoundReference(r.dataType, r.exprId)));
    }

    default List<BoundReference> extractUseSchema(List<Expression> exprs) {
        ImmutableList.Builder<BoundReference> useBuilder = ImmutableList.builder();
        for (Expression item : exprs) {
            item.transformDown((Applicable<Expression>) expr -> {
                if (expr.getClass() == Attribute.class) {
                    Attribute attribute = (Attribute) expr;
                    useBuilder.add(new BoundReference(attribute.dataType, attribute.exprId));
                }
                return expr;
            });
        }
        return useBuilder.build();
    }
}
