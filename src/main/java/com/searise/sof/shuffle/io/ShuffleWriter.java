package com.searise.sof.shuffle.io;

import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.shuffle.MapOutputTracker;
import com.searise.sof.shuffle.MapStatus;

import java.util.List;
import java.util.Objects;

public class ShuffleWriter {
    private final List<Expression> shuffleKeys;
    private final long shuffleId;
    private final int mapId;
    private final MapOutputTracker tracker;
    private final ShuffleStore shuffleStore = new ShuffleStore();
    private final int reduceNum;

    private volatile boolean isCommitted = false;

    public ShuffleWriter(List<Expression> shuffleKeys, long shuffleId, int mapId, MapOutputTracker tracker, int reduceNum) {
        this.shuffleKeys = shuffleKeys;
        this.shuffleId = shuffleId;
        this.mapId = mapId;
        this.tracker = tracker;
        this.reduceNum = reduceNum;
    }

    public void write(InternalRow row) {
        Utils.checkArgument(!isCommitted, "shuffleWriter has committed");

        int reduceId = hashKey(row);
        // row需要copy, 因为写入的internalRow可能还有其他地方修改.
        shuffleStore.write(reduceId, row.copy());
    }

    private int hashKey(InternalRow row) {
        int hashKey = hashKey(row, shuffleKeys, reduceNum);
        Utils.checkArgument(hashKey >= 0 && hashKey < reduceNum,
                String.format("shuffleId(%s), row(%s), keys(%s), hashKey (%s) < 0 || >= " + reduceNum,
                        shuffleId, row, shuffleKeys, hashKey));
        return hashKey;
    }

    // hashKey必须在[0, reduceNum).
    // 和reduceId的范围对应.
    public static int hashKey(InternalRow row, List<Expression> shuffleKeys, int reduceNum) {
        int hashCode = 1;
        for (Expression shuffleKey : shuffleKeys) {
            Object value = shuffleKey.eval(row);
            hashCode = 31 * hashCode + (Objects.isNull(value) ? 0 : value.hashCode());
        }
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        return hashCode % reduceNum;
    }

    // 这个方法只能在dag_scheduler中调用.
    // 因为判断shuffleMapStage的partition完成依赖tracker.registerMapOutput.
    // 在TaskExecutor中调用,会导致stage被错误判断完成.
    public void commit() {
        Utils.checkArgument(!isCommitted, "shuffleWriter has committed");
        isCommitted = true;

        MapStatus mapStatus = new MapStatus(shuffleStore, shuffleId, mapId);
        tracker.registerMapOutput(shuffleId, mapId, mapStatus);
    }
}
