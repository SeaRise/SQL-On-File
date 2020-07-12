package com.searise.sof.execution;

import com.searise.sof.core.Predication;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.JoinRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class NestedLoopJoinExec implements Executor {
    private final Executor stream;
    private final Executor build;
    private final InternalRow output;
    private final Predication predication;
    private final Projection schemaProjection;
    private InternalRow streamRow = EMPTY_ROW;

    public NestedLoopJoinExec(Executor stream, Executor build, List<Expression> conditions, List<BoundReference> schema) {
        this.stream = stream;
        this.build = build;
        this.output = new ArrayRow(schema.size());
        this.predication = new Predication(conditions);
        this.schemaProjection = new Projection(Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)), output);
    }

    @Override
    public void open() {
        stream.open();
        build.open();
        if (stream.hasNext()) {
            streamRow = stream.next();
        }
    }

    @Override
    public boolean hasNext() {
        return streamRow != EMPTY_ROW;
    }

    @Override
    public InternalRow next() {
        if (streamRow == EMPTY_ROW) {
            return EMPTY_ROW;
        }

        if (!build.hasNext()) {
            if (stream.hasNext()) {
                streamRow = stream.next();
                // reset build
                build.close();
                build.open();
                return next();
            }
            streamRow = EMPTY_ROW;
            return EMPTY_ROW;
        }

        InternalRow buildRow = build.next();
        JoinRow joinRow = new JoinRow(streamRow, buildRow);
        if (!predication.apply(joinRow)) {
            return EMPTY_ROW;
        }
        return schemaProjection.apply(joinRow);
    }

    @Override
    public void close() {
        stream.close();
        build.close();
    }
}
