package com.searise.sof.schedule;

import com.searise.sof.execution.Executor;

public abstract class Task {
    public final int stageId;
    public final int partition;
    protected final Executor executor;

    public Task(int stageId, int partition, Executor executor) {
        this.stageId = stageId;
        this.partition = partition;
        this.executor = executor;
    }

    public abstract void runTask();
}
