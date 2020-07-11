package com.searise.sof.expression;

import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.common.SofException;
import com.searise.sof.type.DataType;

public interface Expression extends AnalysisHelper<Expression> {
    default boolean resolved() {
        return children().stream().allMatch(Expression::resolved);
    }

    default DataType dataType() {
        throw new SofException("unresolved expression can not call dataType");
    }
}
