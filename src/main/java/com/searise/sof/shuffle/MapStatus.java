package com.searise.sof.shuffle;

import com.searise.sof.shuffle.io.ShuffleStore;

public class MapStatus {
    public final ShuffleStore shuffleStore;
    public final long shuffleId;
    public final int mapId;

    public MapStatus(ShuffleStore shuffleStore, long shuffleId, int mapId) {
        this.shuffleStore = shuffleStore;
        this.shuffleId = shuffleId;
        this.mapId = mapId;
    }

    public void cleanUp() {
        shuffleStore.cleanUp();
    }
}
