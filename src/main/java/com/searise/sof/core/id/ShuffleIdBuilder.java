package com.searise.sof.core.id;

import java.util.concurrent.atomic.AtomicLong;

public class ShuffleIdBuilder {
    public ShuffleIdBuilder() {
    }

    private AtomicLong curId = new AtomicLong();

    public long newShuffleId() {
        return curId.getAndIncrement();
    }
}
