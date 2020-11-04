package com.searise.sof.schedule.task;

import com.searise.sof.core.Context;
import com.searise.sof.schedule.dag.DagScheduler;

import java.util.List;

// 内部会对单独一个task做重试
// 重试超过一定次数,才会把错误抛出给dagScheduler.
// todo task重试机制, 目前不重试.
public class TaskScheduler {
    private final TaskExecutor taskExecutor;
    private final DagScheduler dagScheduler;

    public TaskScheduler(Context context, DagScheduler dagScheduler) {
        this.taskExecutor = new TaskExecutor(context, this);
        this.dagScheduler = dagScheduler;
    }

    public void submit(List<Task> tasks) {
        for (Task task : tasks) {
            taskExecutor.submit(task);
        }
    }

    public void statusUpdate(TaskResult taskResult) {
        dagScheduler.statusUpdate(taskResult);
    }

    public void stop() {
        taskExecutor.stop();
    }
}
