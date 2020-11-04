package com.searise.sof.schedule.task;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.RowIterator;
import com.searise.sof.shuffle.io.ShuffleWriter;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ShuffleMapTask extends Task {
    private final ShuffleWriter shuffleWriter;

    public ShuffleMapTask(long stageId, int partition, RowIterator rowIterator, ShuffleWriter shuffleWriter) {
        super(stageId, partition, rowIterator);
        this.shuffleWriter = shuffleWriter;
    }

    @Override
    public void runTask() throws Exception {
        rowIterator.open();
        while (rowIterator.hasNext()) {
            InternalRow row = rowIterator.next();
            if (row != EMPTY_ROW) {
                shuffleWriter.write(row);
            }
        }
        rowIterator.close();
        shuffleWriter.commit();
    }
}
