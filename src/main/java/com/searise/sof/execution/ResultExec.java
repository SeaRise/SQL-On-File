package com.searise.sof.execution;

import com.searise.sof.core.Context;
import com.searise.sof.core.row.InternalRow;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ResultExec implements Executor {
    public final Executor child;
    private final StringBuilder resultBuilder = new StringBuilder();
    public final Context context;

    public ResultExec(Executor child, Context context) {
        this.child = child;
        this.context = context;
    }

    @Override
    public void open() {
        long start = System.currentTimeMillis();
        int rowCount = 0;
        child.open();
        while (child.hasNext()) {
            InternalRow row = child.next();
            if (row == EMPTY_ROW) {
                continue;
            }

            for (int index = 0; index < row.numFields() - 1; index++) {
                resultBuilder.append(row.getValue(index)).append("\t");
            }
            resultBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
            rowCount++;
        }
        long end = System.currentTimeMillis();
        long useSecond = (end - start) / 1000;
        resultBuilder.append("Time taken: ").append(useSecond).
                append(" seconds, Fetched: ").append(rowCount).
                append(" row(s)");
    }

    public String result() {
        return resultBuilder.toString();
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
        child.close();
    }
}
