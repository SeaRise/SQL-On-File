package com.searise.sof.optimize.transformation;

import com.google.common.base.Preconditions;
import org.junit.Test;

public class ExploreRecordSuite {

    @Test
    public void test() {
        ExploreRecord record = new ExploreRecord();
        for (int i = 0; i < 10; i++) {
            Preconditions.checkArgument(!record.isExplored(i));
            record.explore(i);
            Preconditions.checkArgument(record.isExplored(i));
        }

        record = new ExploreRecord();
        for (int i = 9; i >= 0; i--) {
            Preconditions.checkArgument(!record.isExplored(i));
            record.explore(i);
            Preconditions.checkArgument(record.isExplored(i));
        }
    }
}
