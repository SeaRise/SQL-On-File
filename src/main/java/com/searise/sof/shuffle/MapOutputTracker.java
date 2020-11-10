package com.searise.sof.shuffle;

import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// thread safe
// shuffleId是shuffle的标识,exchange一一对应
// mapId是写入的task的partition.
// reduceId是读取的task的partition..
public class MapOutputTracker {
    // shuffleId, MapStatus[]
    private final Map<Long, MapStatus[]> tracker = new ConcurrentHashMap<>();

    public MapOutputTracker() {
    }

    public void registerShuffle(long shuffleId, int mapNum) {
        MapStatus[] mapStatuses = new MapStatus[mapNum];
        if (Objects.nonNull(tracker.putIfAbsent(shuffleId, mapStatuses))) {
            throw new SofException(String.format("shuffle(%s) has registered!", shuffleId));
        }
    }

    public void unregisterShuffle(long shuffleId) {
        MapStatus[] mapStatuses = removeShuffle(shuffleId);
        synchronized (mapStatuses) {
            for (int mapId = 0; mapId < mapStatuses.length; mapId++) {
                MapStatus mapStatus = mapStatuses[mapId];
                mapStatus.cleanUp();
                mapStatuses[mapId] = null;
            }
        }
    }

    public void registerMapOutput(long shuffleId, int mapIndex, MapStatus mapStatus) {
        MapStatus[] mapStatuses = getShuffle(shuffleId);
        synchronized (mapStatuses) {
            Utils.checkArgument(Objects.isNull(mapStatuses[mapIndex]),
                    String.format("mapOutput(shuffleId=%s, mapId=%s) has registered", shuffleId, mapIndex));
            mapStatuses[mapIndex] = mapStatus;
        }
    }

    public boolean hasMapOutput(long shuffleId, int mapId) {
        MapStatus[] mapStatuses = getShuffle(shuffleId);
        synchronized (mapStatuses) {
            return Objects.nonNull(mapStatuses[mapId]);
        }
    }

    @SuppressWarnings("unchecked")
    public Iterator<InternalRow>[] getMapOutput(long shuffleId, int reduceId) {
        MapStatus[] mapStatuses = getShuffle(shuffleId);
        synchronized (mapStatuses) {
            if (mapStatuses.length <= 0) {
                return new Iterator[0];
            }
            Iterator<InternalRow>[] iterators = new Iterator[mapStatuses.length];
            for (int i = 0; i < mapStatuses.length; i++) {
                iterators[i] = mapStatuses[i].shuffleStore.read(reduceId);
            }
            return iterators;
        }
    }

    private MapStatus[] getShuffle(long shuffleId) {
        return Utils.checkNotNull(tracker.get(shuffleId),
                String.format("shuffle(%s) has not registered!", shuffleId));
    }

    private MapStatus[] removeShuffle(long shuffleId) {
        return Utils.checkNotNull(tracker.remove(shuffleId),
                String.format("shuffle(%s) has not registered!", shuffleId));
    }

    public void clear() {
        tracker.clear();
    }
}
