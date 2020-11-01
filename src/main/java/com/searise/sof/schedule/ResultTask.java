package com.searise.sof.schedule;

import com.searise.sof.execution.Executor;
import com.searise.sof.schedule.dag.ResultHandle;

public class ResultTask extends Task {
    private final ResultHandle resultHandle;

    public ResultTask(int stageId, int partition, Executor executor, ResultHandle resultHandle) {
        super(stageId, partition, executor);
        this.resultHandle = resultHandle;
    }

    @Override
    public void runTask() {
        executor.open();
        while (executor.hasNext()) {
            resultHandle.handle(executor.next());
        }
        executor.close();
    }
}
