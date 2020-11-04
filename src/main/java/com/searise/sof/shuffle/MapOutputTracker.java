package com.searise.sof.shuffle;

import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// thread safe
public class MapOutputTracker {
    public final Context context;

    // shuffleId, MapStatus[]
    private final Map<Long, MapStatus[]> tracker = new ConcurrentHashMap<>();

    public MapOutputTracker(Context context) {
        this.context = context;
    }

    public void registerShuffle(long shuffleId, int mapNum) {
        tracker.put(shuffleId, new MapStatus[mapNum]);
    }

    public void removeShuffle(long shuffleId) {
        MapStatus[] mapStatuses = getShuffle(shuffleId);
        synchronized (mapStatuses) {
            for (MapStatus mapStatus : getShuffle(shuffleId)) {
                mapStatus.cleanUp();
            }
        }
    }

    public void registerMapOutput(long shuffleId, int mapIndex, MapStatus mapStatus) {
        MapStatus[] mapStatuses = getShuffle(shuffleId);
        synchronized (mapStatuses) {
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
                String.format("shuffle(%s) has not register!", shuffleId));
    }
}