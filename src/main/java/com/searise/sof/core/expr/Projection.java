package com.searise.sof.core.expr;

import com.searise.sof.core.SofContext;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;

import java.util.List;

public class Projection extends MutableProjection {
    public Projection(List<Expression> exprs, InternalRow output, SofContext context) {
        super(exprs, context);
        this.output = output;
    }

    public void target(InternalRow output) {
        throw new UnsupportedOperationException("can't call target in Projection");
    }
}
