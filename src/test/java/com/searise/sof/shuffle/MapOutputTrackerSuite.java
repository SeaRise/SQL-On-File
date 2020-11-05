package com.searise.sof.shuffle;

import org.junit.Test;

public class MapOutputTrackerSuite {

    @Test
    public void test() {
        MapOutputTracker tracker = new MapOutputTracker();
        long shuffleId = 0;
        int partitions = 10;

        tracker.registerShuffle(shuffleId, partitions);



        tracker.removeShuffle(shuffleId);
    }
}
