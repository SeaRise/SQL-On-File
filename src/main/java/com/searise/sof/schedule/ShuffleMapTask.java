package com.searise.sof.schedule;

import com.searise.sof.execution.RowIterator;

public class ShuffleMapTask extends Task {
    public ShuffleMapTask(int stageId, int partition, RowIterator rowIterator) {
        super(stageId, partition, rowIterator);
    }

    @Override
    public void runTask() {
        rowIterator.open();
        while (rowIterator.hasNext()) {
            rowIterator.next();
        }
        rowIterator.close();
    }
}
