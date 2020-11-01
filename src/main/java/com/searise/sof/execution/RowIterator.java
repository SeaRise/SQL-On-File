package com.searise.sof.execution;

import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class RowIterator implements Iterator<InternalRow> {
    public void open() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public InternalRow next() {
        return EMPTY_ROW;
    }

    public void close() {

    }
}
