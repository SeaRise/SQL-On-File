package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Predication;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.JoinRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class NestedLoopJoinExec implements Executor {
    private final Executor stream;
    private final Executor build;
    private final Predication predication;
    private final Projection schemaProjection;
    private InternalRow streamRow = EMPTY_ROW;
    public final Context context;

    public NestedLoopJoinExec(Executor stream, Executor build, List<Expression> conditions, List<BoundReference> schema, Context context) {
        this.stream = stream;
        this.build = build;
        this.context = context;
        InternalRow output = new ArrayRow(schema.size());
        this.predication = new Predication(conditions, context);
        this.schemaProjection = new Projection(Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)), output, context);
    }

    private NestedLoopJoinExec(Executor stream, Executor build, Predication predication, Projection schemaProjection, Context context) {
        this.stream = stream;
        this.build = build;
        this.predication = predication;
        this.schemaProjection = schemaProjection;
        this.context = context;
    }

    @Override
    public void open() {
        stream.open();
        build.open();
        while (stream.hasNext()) {
            streamRow = stream.next();
            if (streamRow != EMPTY_ROW) {
                return;
            }
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
            while (stream.hasNext()) {
                streamRow = stream.next();
                if (streamRow == EMPTY_ROW) {
                    continue;
                }
                // reset build
                build.close();
                build.open();
                return next();
            }
            streamRow = EMPTY_ROW;
            return EMPTY_ROW;
        }

        InternalRow buildRow = build.next();
        while (buildRow == EMPTY_ROW && build.hasNext()) {
            buildRow = build.next();
        }
        if (buildRow == EMPTY_ROW) {
            return EMPTY_ROW;
        }

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

    @Override
    public List<Executor> children() {
        return ImmutableList.of(stream, build);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new NestedLoopJoinExec(stream, build, predication, schemaProjection, context);
    }
}
