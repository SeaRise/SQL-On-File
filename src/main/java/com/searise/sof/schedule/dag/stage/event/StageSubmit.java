package com.searise.sof.schedule.dag.stage.event;

import com.searise.sof.schedule.dag.stage.Stage;

public class StageSubmit implements Event {
    public final Stage stage;

    public StageSubmit(Stage stage) {
        this.stage = stage;
    }

    @Override
    public String toString() {
        return String.format("StageSubmit(stageId=%s)", stage.stageId);
    }
}
