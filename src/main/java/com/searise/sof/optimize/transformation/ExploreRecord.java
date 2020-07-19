package com.searise.sof.optimize.transformation;

public class ExploreRecord {
    private int record = 0;

    public void explore(int index) {
        record |= (1 << index);
    }

    public boolean isExplored(int index) {
        return (record & (1 << index)) != 0;
    }

    public ExploreRecord copy() {
        ExploreRecord exploreRecord = new ExploreRecord();
        exploreRecord.record = record;
        return exploreRecord;
    }
}
