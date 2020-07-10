package com.searise.sof.expression;

import com.searise.sof.analyse.AnalysisHelper;

public interface Expression extends AnalysisHelper<Expression> {
    default boolean resolved() {
        return children().stream().allMatch(Expression::resolved);
    }
}
