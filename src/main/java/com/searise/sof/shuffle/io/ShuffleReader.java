package com.searise.sof.shuffle.io;

import com.google.common.collect.Iterators;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.shuffle.MapOutputTracker;

import java.util.Iterator;

public class ShuffleReader {
    private final MapOutputTracker tracker;
    private final long shuffleId;
    private final int reduceId;

    public ShuffleReader(MapOutputTracker tracker, long shuffleId, int reduceId) {
        this.tracker = tracker;
        this.shuffleId = shuffleId;
        this.reduceId = reduceId;
    }

    public Iterator<InternalRow> iterator() {
        Iterator<InternalRow>[] iterators = tracker.getMapOutput(shuffleId, reduceId);
        return Iterators.concat(iterators);
    }
}
