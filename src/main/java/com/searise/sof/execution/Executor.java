package com.searise.sof.execution;

import java.util.List;

public interface Executor {
    RowIterator compute(int partition);

    List<Executor> children();

    Executor copyWithNewChildren(List<Executor> children);
}
