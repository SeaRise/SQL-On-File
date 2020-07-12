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
        throw new SofException("unresolved expression can not call dataType");
    }

    default Object eval(InternalRow input) {
        throw new SofException("can not support eval");
    }
}
