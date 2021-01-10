package com.searise.sof.shuffle;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.shuffle.io.ShuffleStore;

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
    private final Map<Long, ShuffleStore> shuffleOutputs = new ConcurrentHashMap<>();

    public MapOutputTracker() {
    }

    public void registerShuffle(long shuffleId, int mapNum, int reduceNum) {
        MapStatus[] mapStatuses = new MapStatus[mapNum];
        if (Objects.nonNull(tracker.putIfAbsent(shuffleId, mapStatuses))) {
            throw new SofException(String.format("shuffle(%s) has registered!", shuffleId));
        }
        shuffleOutputs.put(shuffleId, new ShuffleStore(reduceNum));
    }

    public void unregisterShuffle(long shuffleId) {
        MapStatus[] mapStatuses = removeShuffle(shuffleId);
        synchronized (mapStatuses) {
            for (int mapId = 0; mapId < mapStatuses.length; mapId++) {
                mapStatuses[mapId] = null;
            }
        }
        shuffleOutputs.remove(shuffleId).close();
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

    public ShuffleStore getShuffleStore(long shuffleId) {
        return Preconditions.checkNotNull(shuffleOutputs.get(shuffleId));
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
