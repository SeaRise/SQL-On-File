package com.searise.sof.shuffle.io;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.shuffle.MapOutputTracker;
import com.searise.sof.shuffle.MapStatus;

import java.util.List;

public class ShuffleWriter {
    private final List<Expression> shuffleKeys;
    private final long shuffleId;
    private final int mapId;
    private final MapOutputTracker tracker;
    private final ShuffleStore shuffleStore = new ShuffleStore();
    private final int reduceNum;

    public ShuffleWriter(List<Expression> shuffleKeys, long shuffleId, int mapId, MapOutputTracker tracker, int reduceNum) {
        this.shuffleKeys = shuffleKeys;
        this.shuffleId = shuffleId;
        this.mapId = mapId;
        this.tracker = tracker;
        this.reduceNum = reduceNum;
    }

    public void write(InternalRow row) {
        int reduceId = hashKey(row);
        // row需要copy, 因为写入的internalRow可能还有其他地方修改.
        shuffleStore.write(reduceId, row.copy());
    }

    private int hashKey(InternalRow row) {
        int hashCode = 1;
        for (Expression shuffleKey : shuffleKeys) {
            Object value = shuffleKey.eval(row);
            hashCode = 31 * hashCode + (value==null ? 0 : value.hashCode());
        }
        return hashCode % reduceNum;
    }

    public void commit() {
        MapStatus mapStatus = new MapStatus(shuffleStore, shuffleId, mapId);
        tracker.registerMapOutput(shuffleId, mapId, mapStatus);
    }
}
