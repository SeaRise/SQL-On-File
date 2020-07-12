package com.searise.sof.execution;

import com.searise.sof.core.row.InternalRow;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class PrintlnExec implements Executor {
    private final Executor child;
    private final StringBuilder printlnBuilder = new StringBuilder();
    public PrintlnExec(Executor child) {
        this.child = child;
    }

    @Override
    public void open() {
        child.open();
        while (child.hasNext()) {
            InternalRow row = child.next();
            if (row == EMPTY_ROW) {
                continue;
            }

            for (int index = 0; index < row.numFields()-1; index++) {
                printlnBuilder.append(row.getValue(index)).append(",");
            }
            printlnBuilder.append(row.getValue(row.numFields()-1)).append("\n");
            System.out.println(printlnBuilder.toString());
            printlnBuilder.setLength(0);
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
        child.close();
    }
}
