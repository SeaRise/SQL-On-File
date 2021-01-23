package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.expr.MutableProjection;
import com.searise.sof.core.expr.Predication;
import com.searise.sof.core.expr.Projection;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.JoinRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class HashJoinExec implements Executor {
    private final Executor stream;
    private final Executor build;

    public final List<Expression> streamJoinKeys;
    public final List<Expression> buildJoinKeys;
    public final List<Expression> otherConditions;
    public final List<BoundReference> schema;

    public final SofContext context;

    public HashJoinExec(Executor stream, Executor build, List<Expression> streamJoinKeys, List<Expression> buildJoinKeys,
                        List<Expression> otherConditions, List<BoundReference> schema, SofContext context) {
        this.stream = stream;
        this.build = build;

        this.streamJoinKeys = streamJoinKeys;
        this.buildJoinKeys = buildJoinKeys;
        this.otherConditions = otherConditions;
        this.schema = schema;

        this.context = context;
    }

    @Override
    public RowIterator compute(int partition) {
        RowIterator streamRowIterator = stream.compute(partition);
        RowIterator buildRowIterator = build.compute(partition);

        return new RowIterator() {
            private final Predication predication = new Predication(otherConditions, context);
            private final Projection schemaProjection = new Projection(
                    Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)),
                    new ArrayRow(schema.size()), context);
            private final MutableProjection streamKeyProjection = new MutableProjection(streamJoinKeys, context);
            private final MutableProjection buildKeyProjection = new MutableProjection(buildJoinKeys, context);
            private final HashRelation hashRelation = new HashRelation(context, buildKeyProjection);
            private InternalRow streamRow = EMPTY_ROW;
            private Iterator<InternalRow> hitIter;

            @Override
            public void open() {
                streamRowIterator.open();
                buildRowIterator.open();

                buildHasRelation(buildRowIterator, hashRelation);
                while (streamRowIterator.hasNext()) {
                    streamRow = streamRowIterator.next();
                    if (streamRow != EMPTY_ROW) {
                        break;
                    }
                }

                if (streamRow == EMPTY_ROW) {
                    return;
                }
                InternalRow streamKeyRow = streamKeyProjection.produce(streamRow);
                hitIter = hashRelation.get(streamKeyRow);
            }

            @Override
            public boolean hasNext() {
                return !hashRelation.isEmpty() && streamRow != EMPTY_ROW;
            }

            @Override
            public InternalRow next() {
                if (hashRelation.isEmpty() || streamRow == EMPTY_ROW) {
                    return EMPTY_ROW;
                }

                if (!hitIter.hasNext()) {
                    while (streamRowIterator.hasNext()) {
                        streamRow = streamRowIterator.next();
                        if (streamRow == EMPTY_ROW) {
                            continue;
                        }
                        InternalRow streamKeyRow = streamKeyProjection.produce(streamRow);
                        hitIter = hashRelation.get(streamKeyRow);
                        return next();
                    }
                    streamRow = EMPTY_ROW;
                    return EMPTY_ROW;
                }

                InternalRow buildRow = hitIter.next();
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

    private void buildHasRelation(RowIterator build, HashRelation hashRelation) {
        while (build.hasNext()) {
            InternalRow buildRow = build.next();
            if (buildRow == EMPTY_ROW) {
                continue;
            }

            hashRelation.append(buildRow);
        }
        build.close();
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of(stream, build);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new HashJoinExec(stream, build, streamJoinKeys, buildJoinKeys, otherConditions, schema, context);
    }
}
