package com.searise.sof.plan.physics;

import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.ArrayList;
import java.util.List;

public interface SchemaPruneHelper {
    void prune(List<BoundReference> father, boolean isTop);

    default List<BoundReference> extractUseSchema(List<Expression> exprs) {
        List<BoundReference> useBuilder = new ArrayList<>();
        for (Expression item : exprs) {
            item.transformUp((Applicable<Expression>) expr -> {
                if (expr.getClass() == Attribute.class) {
                    Attribute attribute = (Attribute) expr;
                    useBuilder.add(new BoundReference(attribute.dataType, attribute.exprId));
                }
                return expr;
            });
        }
        return Utils.toImmutableList(useBuilder.stream().distinct());
    }
}
