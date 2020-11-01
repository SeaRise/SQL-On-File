package com.searise.sof.schedule.split;

public class ShuffleSplit implements Split {
    public final long shuffleId;
    public final long reduceId;

    public ShuffleSplit(long shuffleId, long reduceId) {
        this.shuffleId = shuffleId;
        this.reduceId = reduceId;
    }
}