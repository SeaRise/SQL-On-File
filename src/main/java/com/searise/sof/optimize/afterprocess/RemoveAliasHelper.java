package com.searise.sof.optimize.afterprocess;

import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;

import java.util.List;

public interface RemoveAliasHelper {
    default void removeAlias() {
    }

    static List<Expression> doRemoveAlias(List<Expression> expressions) {
        return Utils.toImmutableList(expressions.stream().map(expression -> expression.transformUp((Applicable<Expression>) expr -> {
            if (expr.getClass() == Alias.class) {
                return ((Alias) expr).child;
            }
            return expr;
        })));
    }
}
