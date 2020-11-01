package com.searise.sof.schedule.dag;

import com.searise.sof.core.row.InternalRow;

@FunctionalInterface
public interface ResultHandle {
    void handle(InternalRow row);
}
