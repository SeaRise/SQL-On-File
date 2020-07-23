package com.searise.sof.execution;

import com.searise.sof.core.Context;
import com.searise.sof.core.row.InternalRow;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ResultExec implements Executor {
    private final Executor child;
    private final StringBuilder resultBuilder = new StringBuilder();
    public final Context context;

    public ResultExec(Executor child, Context context) {
        this.child = child;
        this.context = context;
    }

    @Override
    public void open() {
        child.open();
        while (child.hasNext()) {
            InternalRow row = child.next();
            if (row == EMPTY_ROW) {
                continue;
            }

            for (int index = 0; index < row.numFields() - 1; index++) {
                resultBuilder.append(row.getValue(index)).append(",");
            }
            resultBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
        }
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
