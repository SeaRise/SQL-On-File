package com.searise.sof.schedule.dag;

import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;

@FunctionalInterface
public interface ResultHandle {
    void handle(int partition, Iterator<InternalRow> iterator) throws Exception;
}
