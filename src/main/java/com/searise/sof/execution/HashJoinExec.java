package com.searise.sof.execution;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.searise.sof.core.MutableProjection;
import com.searise.sof.core.Predication;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.JoinRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.Iterator;
import java.util.List;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class HashJoinExec implements Executor {
    private final Executor stream;
    private final Executor build;
    private final Predication predication;
    private final Projection schemaProjection;
    private final MutableProjection streamKeyProjection;
    private final MutableProjection buildKeyProjection;
    private Multimap<InternalRow, InternalRow> buildMap = ImmutableListMultimap.of();
    private InternalRow streamRow = EMPTY_ROW;
    private Iterator<InternalRow> hitIter;

    public HashJoinExec(Executor stream, Executor build, List<Expression> streamJoinKeys, List<Expression> buildJoinKeys,
                        List<Expression> otherConditions, List<BoundReference> schema) {
        this.stream = stream;
        this.build = build;
        InternalRow output = new ArrayRow(schema.size());
        this.schemaProjection = new Projection(Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)), output);

        this.predication = new Predication(otherConditions);
        streamKeyProjection = new MutableProjection(streamJoinKeys);
        buildKeyProjection = new MutableProjection(buildJoinKeys);
    }

    @Override
    public void open() {
        stream.open();
        build.open();
        buildHashMap();

        while (stream.hasNext()) {
            streamRow = stream.next();
            if (streamRow != EMPTY_ROW) {
                break;
            }
        }

        InternalRow streamKeyRow = getKeyRow(streamRow, streamKeyProjection);
        hitIter = buildMap.get(streamKeyRow).iterator();
    }

    private void buildHashMap() {
        ImmutableListMultimap.Builder<InternalRow, InternalRow> builder = ImmutableListMultimap.builder();
        while (build.hasNext()) {
            InternalRow buildRow = build.next();
            if (buildRow == EMPTY_ROW) {
                continue;
            }

            InternalRow buildKeyRow = getKeyRow(buildRow, buildKeyProjection);
            builder.put(buildKeyRow, buildRow.copy());
        }
        buildMap = builder.build();
        build.close();
    }

    private InternalRow getKeyRow(InternalRow input, MutableProjection keyProjection) {
        if (keyProjection.size() == 0) {
            return EMPTY_ROW;
        }

        InternalRow keyRow = new ArrayRow(keyProjection.size());
        buildKeyProjection.target(keyRow);
        return buildKeyProjection.apply(input);
    }

    @Override
    public boolean hasNext() {
        return !buildMap.isEmpty() && streamRow != EMPTY_ROW;
    }

    @Override
    public InternalRow next() {
        if (buildMap.isEmpty() || streamRow == EMPTY_ROW) {
            return EMPTY_ROW;
        }

        if (!hitIter.hasNext()) {
            if (stream.hasNext()) {
                streamRow = stream.next();
                InternalRow streamKeyRow = getKeyRow(streamRow, streamKeyProjection);
                hitIter = buildMap.get(streamKeyRow).iterator();
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
//        System.out.println(joinRow);
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
