package com.searise.sof.execution;

import com.searise.sof.core.Predication;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class FilterExec implements Executor {

    private final Executor child;
    private final Projection schemaProjection;
    private final Predication predication;

    public FilterExec(Executor child, List<Expression> conditions, List<BoundReference> schema) {
        this.child = child;
        InternalRow output = new ArrayRow(schema.size());
        this.schemaProjection = new Projection(Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)), output);
        this.predication = new Predication(conditions);
    }

    @Override
    public void open() {
        child.open();
    }

    @Override
    public boolean hasNext() {
        return child.hasNext();
    }

    @Override
    public InternalRow next() {
        InternalRow input = child.next();
        if (input == EMPTY_ROW) {
            return EMPTY_ROW;
        }

        if (!predication.apply(input)) {
            return EMPTY_ROW;
        }

        return schemaProjection.apply(input);
    }

    @Override
    public void close() {
        child.close();
    }
}
