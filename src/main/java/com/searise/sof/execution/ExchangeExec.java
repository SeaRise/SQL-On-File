package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.shuffle.io.ShuffleReader;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ExchangeExec implements Executor {
    public final long shuffleId;
    public final Context context;

    public ExchangeExec(long shuffleId, Context context) {
        this.shuffleId = shuffleId;
        this.context = context;
    }

    @Override
    public RowIterator compute(int partition) {
        return new RowIterator() {
            private final ShuffleReader shuffleReader = new ShuffleReader(context.mapOutputTracker, shuffleId, partition);
            private Iterator<InternalRow> iterator;

            @Override
            public void open() {
                iterator = shuffleReader.iterator();
            }

            @Override
            public boolean hasNext() {
                return Objects.nonNull(iterator) && iterator.hasNext();
            }

            @Override
            public InternalRow next() {
                return iterator.next();
            }

            @Override
            public void close() {
                iterator = null;
            }
        };
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of();
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.isEmpty());
        return this;
    }
}
