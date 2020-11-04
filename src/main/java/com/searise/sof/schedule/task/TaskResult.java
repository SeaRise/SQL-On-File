package com.searise.sof.schedule.task;

public class TaskResult {
    public final Throwable throwable;
    public final TaskStatus taskStatus;
    public final long stageId;
    public final int partition;

    private TaskResult(Throwable throwable, TaskStatus taskStatus, long stageId, int partition) {
        this.throwable = throwable;
        this.taskStatus = taskStatus;
        this.stageId = stageId;
        this.partition = partition;
    }

    public static TaskResult success(long stageId, int partition) {
        return new TaskResult(null, TaskStatus.SUCCESS, stageId, partition);
    }

    public static TaskResult fail(Throwable throwable, long stageId, int partition) {
        return new TaskResult(null, TaskStatus.FAIL, stageId, partition);
    }
}
