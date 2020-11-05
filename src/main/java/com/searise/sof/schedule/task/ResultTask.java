package com.searise.sof.schedule.task;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.RowIterator;
import com.searise.sof.schedule.dag.ResultHandle;

import java.util.Iterator;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ResultTask extends Task {
    private final ResultHandle resultHandle;

    public ResultTask(long stageId, int partition, RowIterator rowIterator, ResultHandle resultHandle) {
        super(stageId, partition, rowIterator);
        this.resultHandle = resultHandle;
    }

    @Override
    public void runTask() throws Exception {
        rowIterator.open();
        resultHandle.handle(partition, new Iterator<InternalRow>() {
            private InternalRow row = EMPTY_ROW;
            @Override
            public boolean hasNext() {
                while (rowIterator.hasNext() && row == EMPTY_ROW) {
                    row = rowIterator.next();
                }
                return row != EMPTY_ROW;
            }
            @Override
            public InternalRow next() {
                InternalRow cur = row;
                row = EMPTY_ROW;
                return cur;
            }
        });
        rowIterator.close();
    }
}
