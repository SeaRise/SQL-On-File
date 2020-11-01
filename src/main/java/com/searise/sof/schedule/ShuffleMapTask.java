package com.searise.sof.schedule;

import com.searise.sof.execution.Executor;

public class ShuffleMapTask extends Task {
    public ShuffleMapTask(int stageId, int partition, Executor executor) {
        super(stageId, partition, executor);
    }

    @Override
    public void runTask() {
        executor.open();
        while (executor.hasNext()) {
            executor.next();
        }
        executor.close();
    }
}
