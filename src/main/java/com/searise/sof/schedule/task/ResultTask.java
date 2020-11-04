package com.searise.sof.schedule.task;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.RowIterator;
import com.searise.sof.schedule.dag.ResultHandle;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ResultTask extends Task {
    private final ResultHandle resultHandle;

    public ResultTask(long stageId, int partition, RowIterator rowIterator, ResultHandle resultHandle) {
        super(stageId, partition, rowIterator);
        this.resultHandle = resultHandle;
    }

    @Override
    public void runTask() {
        rowIterator.open();
        while (rowIterator.hasNext()) {
            InternalRow row = rowIterator.next();
            if (row != EMPTY_ROW) {
                resultHandle.handle(row);
            }
        }
        rowIterator.close();
    }
}
