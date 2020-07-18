package com.searise.sof.expression;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.type.DataType;

import java.util.List;

public interface Expression extends AnalysisHelper<Expression> {
    default boolean resolved() {
        return children().stream().allMatch(Expression::resolved);
    }

    default DataType dataType() {
        throw new SofException(String.format("unresolved expression %s can not call dataType", getClass().getSimpleName()));
    }

    // todo 实现codegen.
    default Object eval(InternalRow input) {
        throw new SofException(String.format("%s can not support eval", getClass().getSimpleName()));
    }

    // todo 实现foldable接口.
    default boolean foldable() {
        return false;
    }

    static List<Attribute> getUseAttributes(Expression expression) {
        ImmutableList.Builder<Attribute> builder = ImmutableList.builder();
        expression.transformUp((Applicable<Expression>) expr -> {
            if (expr.getClass() == Attribute.class) {
                builder.add((Attribute) expr);
            }
            return expr;
        });
        return builder.build();
    }
}
