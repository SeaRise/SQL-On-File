package com.searise.sof.stats;

import java.math.BigInteger;

public class Statistics {
    public final BigInteger sizeInBytes;

    public Statistics(BigInteger sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public String toString() {
        return String.format("stats [sizeInBytes: %s]", sizeInBytes);
    }

    public static Statistics EMPTY = new Statistics(new BigInteger("0"));
}
