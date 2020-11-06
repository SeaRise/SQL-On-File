package com.searise.sof.schedule.dag.stage.event;

import com.searise.sof.schedule.task.TaskResult;

public class TaskStatusUpdate implements Event {
    public final TaskResult taskResult;

    public TaskStatusUpdate(TaskResult taskResult) {
        this.taskResult = taskResult;
    }

    @Override
    public String toString() {
        return String.format("TaskStatusUpdate(stageId=%s,partition=%s,taskStatus=%s)",
                taskResult.stageId, taskResult.partition, taskResult.taskStatus);
    }
}
