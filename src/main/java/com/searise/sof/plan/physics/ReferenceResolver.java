package com.searise.sof.plan.physics;

import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReferenceResolver {
    private ReferenceResolver() {
    }

    private static Applicable<Expression> resolver(Map<Long, Integer> inputs) {
        return expr -> {
            if (expr.getClass() == Attribute.class) {
                Attribute attribute = (Attribute) expr;
                Integer index = inputs.get(attribute.exprId);
                if (Objects.isNull(index)) {
                    throw new SofException("can not resolveExpression index for " + attribute);
                }
                BoundReference reference = new BoundReference(attribute.dataType, attribute.exprId);
                reference.resolveIndex(index);
                return reference;
            }
            return expr;
        };
    }

    public static List<Expression> resolveExpression(List<Expression> expressions, Map<Long, Integer> inputs) {
        Applicable<Expression> resolver = resolver(inputs);
        return Utils.toImmutableList(expressions.stream().map(expression -> expression.transformDown(resolver)));
    }

    public static void resolveSchema(List<BoundReference> schema, Map<Long, Integer> inputs) {
        for (BoundReference reference : schema) {
            Integer index = inputs.get(reference.exprId);
            if (Objects.isNull(index)) {
                throw new SofException("can not resolveExpression index for " + reference);
            }
            reference.resolveIndex(index);
        }
    }
}
