package com.searise.sof.schedule;

import com.searise.sof.execution.RowIterator;

public abstract class Task {
    public final int stageId;
    public final int partition;
    protected final RowIterator rowIterator;

    public Task(int stageId, int partition, RowIterator rowIterator) {
        this.stageId = stageId;
        this.partition = partition;
        this.rowIterator = rowIterator;
    }

    public abstract void runTask();
}
