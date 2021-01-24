package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.expr.Predication;
import com.searise.sof.core.expr.Projection;
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
    public final List<Expression> conditions;
    public final List<BoundReference> schema;
    public final SofContext context;

    public NestedLoopJoinExec(Executor stream, Executor build, List<Expression> conditions, List<BoundReference> schema, SofContext context) {
        this.stream = stream;
        this.build = build;
        this.conditions = conditions;
        this.schema = schema;
        this.context = context;
    }

    @Override
    public RowIterator compute(int partition) {
        RowIterator streamRowIterator = stream.compute(partition);
        RowIterator buildRowIterator = build.compute(partition);

        return new RowIterator() {
            private Predication predication = new Predication(conditions, context);
            private Projection schemaProjection = new Projection(
                    Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)),
                    new ArrayRow(schema.size()), context);
            private InternalRow streamRow = EMPTY_ROW;

            @Override
            public void open() {
                streamRowIterator.open();
                buildRowIterator.open();
                while (streamRowIterator.hasNext()) {
                    streamRow = streamRowIterator.next();
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

                if (!buildRowIterator.hasNext()) {
                    while (streamRowIterator.hasNext()) {
                        streamRow = streamRowIterator.next();
                        if (streamRow == EMPTY_ROW) {
                            continue;
                        }
                        // reset build
                        buildRowIterator.close();
                        buildRowIterator.open();
                        return next();
                    }
                    streamRow = EMPTY_ROW;
                    return EMPTY_ROW;
                }

                InternalRow buildRow = buildRowIterator.next();
                while (buildRow == EMPTY_ROW && buildRowIterator.hasNext()) {
                    buildRow = buildRowIterator.next();
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
                streamRowIterator.close();
                buildRowIterator.close();
            }
        };
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of(stream, build);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new NestedLoopJoinExec(stream, build, conditions, schema, context);
    }
}
