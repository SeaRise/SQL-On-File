package com.searise.sof.core;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;

import java.util.List;

public class Projection {
    private final List<Expression> exprs;
    private final InternalRow output;

    public Projection(List<Expression> exprs, InternalRow output) {
        this.exprs = exprs;
        this.output = output;
    }

    public InternalRow apply(InternalRow input) {
        for (int index = 0; index < exprs.size(); index++) {
            Expression projectExpr = exprs.get(index);
            InternalRowWriter writer = InternalRow.getWriter(index, projectExpr.dataType());
            writer.apply(output, projectExpr.eval(input));
        }
        return output;
    }
}
