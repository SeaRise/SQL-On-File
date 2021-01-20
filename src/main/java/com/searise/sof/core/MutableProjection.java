package com.searise.sof.core;

import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.core.conf.Conf;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;

import java.util.List;

public class MutableProjection {
    private final List<Expression> exprs;
    protected InternalRow output;
    public final Context context;

    public MutableProjection(List<Expression> exprs, Context context) {
        this.context = context;

        if (context.conf.getConf(Conf.CODEGEN_EXPRESSION)) {
            this.exprs = Utils.toImmutableList(exprs.stream().map(CodeGenerator::tryCodegen));
        } else {
            this.exprs = exprs;
        }
    }

    public int size() {
        return exprs.size();
    }

    public void target(InternalRow output) {
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
