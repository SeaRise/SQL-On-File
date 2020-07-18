package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public interface PushFilterDownHelper {
    // left: retain
    // right: pushDown
    default Pair<List<Expression>, List<Expression>> split(List<Expression> expressions, Set<Long> exprIds) {
        ImmutableList.Builder<Expression> retainBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> pushDownBuilder = ImmutableList.builder();
        for (Expression expression : expressions) {
            boolean isRetain = false;
            List<Attribute> useAttributes = Expression.getUseAttributes(expression);
            for (Attribute useAttribute : useAttributes) {
                if (!exprIds.contains(useAttribute.exprId)) {
                    isRetain = true;
                    break;
                }
            }
            if (isRetain) {
                retainBuilder.add(expression);
            } else {
                pushDownBuilder.add(expression);
            }
        }
        return Pair.of(retainBuilder.build(), pushDownBuilder.build());
    }
}
