package com.searise.sof.execution;

import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;

public interface RowIterator extends Iterator<InternalRow> {
    void open();

    @Override
    boolean hasNext();

    @Override
    InternalRow next();

    void close();
}
