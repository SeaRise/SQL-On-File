package com.searise.sof.expression;

import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.type.DataType;

public interface Expression extends AnalysisHelper<Expression> {
    default boolean resolved() {
        return children().stream().allMatch(Expression::resolved);
    }

    default DataType dataType() {
        throw new SofException(String.format("unresolved expression %s can not call dataType", getClass().getSimpleName()));
    }

    default Object eval(InternalRow input) {
        throw new SofException(String.format("%s can not support eval", getClass().getSimpleName()));
    }
}
