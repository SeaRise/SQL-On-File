package com.searise.sof.execution;

import com.searise.sof.core.row.InternalRow;

import java.util.List;

public interface Executor {
    void bindPartition(int partition);

    void open();

    boolean hasNext();

    InternalRow next();

    void close();

    List<Executor> children();

    Executor copyWithNewChildren(List<Executor> children);
}
