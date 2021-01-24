package com.searise.sof.core.expr;

import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;

import java.util.List;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class MutableProjection {
    private final List<Expression> exprs;
    protected InternalRow output;
    public final SofContext context;

    public final int defaultSize;

    public MutableProjection(List<Expression> exprs, SofContext context) {
        this.context = context;

        if (context.conf.getConf(SofConf.CODEGEN_EXPRESSION)) {
            this.exprs = Utils.toImmutableList(exprs.stream().map(CodeGenerator::tryCodegen));
        } else {
            this.exprs = exprs;
        }

        this.defaultSize = exprs.stream().mapToInt(expr -> expr.dataType().defaultJVMSize).sum();
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

    public InternalRow produce(InternalRow input) {
        if (size() == 0) {
            return EMPTY_ROW;
        }

        InternalRow keyRow = new ArrayRow(size());
        target(keyRow);
        return apply(input);
    }
}
