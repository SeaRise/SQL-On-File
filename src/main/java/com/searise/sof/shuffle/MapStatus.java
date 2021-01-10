package com.searise.sof.shuffle;

public class MapStatus {
    public final long shuffleId;
    public final int mapId;

    public MapStatus(long shuffleId, int mapId) {
        this.shuffleId = shuffleId;
        this.mapId = mapId;
    }
}
