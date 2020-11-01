package com.searise.sof.shuffle;

import com.searise.sof.core.Context;

public class MapOutputTracker {
    public final Context context;

    public MapOutputTracker(Context context) {
        this.context = context;
    }

    public void registerShuffle(long shuffleId, int mapNum) {

    }

    public void registerMapOutput(long shuffleId, long mapIndex, MapStatus mapStatus) {

    }
}
