package com.searise.sof.shuffle.io;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;

public class ShuffleStore {
    private Multimap<Integer, InternalRow> storeMap = HashMultimap.create();

    public void write(int partition, InternalRow internalRow) {
        storeMap.put(partition, internalRow);
    }

    public Iterator<InternalRow> read(int partition) {
        return storeMap.get(partition).iterator();
    }

    public void cleanUp() {
        storeMap.clear();
    }
}
