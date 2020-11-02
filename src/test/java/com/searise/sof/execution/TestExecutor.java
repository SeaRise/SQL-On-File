package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.row.InternalRow;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class TestExecutor implements Executor {
    private final Executor child;
    private final StringBuffer resultBuilder = new StringBuffer();
    public final Context context;

    public TestExecutor(Executor child, Context context) {
        this.child = child;
        this.context = context;
    }

    @Override
    public RowIterator compute(int partition) {
        RowIterator childRowIterator = child.compute(partition);
        return new RowIterator() {
            private StringBuilder partitionResultBuilder = new StringBuilder();
            @Override
            public void open() {
                childRowIterator.open();
                while (childRowIterator.hasNext()) {
                    InternalRow row = childRowIterator.next();
                    if (row == EMPTY_ROW) {
                        continue;
                    }

                    for (int index = 0; index < row.numFields() - 1; index++) {
                        partitionResultBuilder.append(row.getValue(index)).append(",");
                    }
                    partitionResultBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
                }
            }
            @Override
            public boolean hasNext() {
                return false;
            }
            @Override
            public InternalRow next() {
                return EMPTY_ROW;
            }
            @Override
            public void close() {
                childRowIterator.close();
                resultBuilder.append(partitionResultBuilder.toString());
                partitionResultBuilder = null;
            }
        };
    }

    public String result() {
        return resultBuilder.toString();
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of(child);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new TestExecutor(child, context);
    }
}
