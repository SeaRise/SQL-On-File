package com.searise.sof.storge.disk;

import org.junit.Test;

public class DiskManagerSuite {
    @Test
    public void test() throws Exception {
        DiskManager diskManager = new DiskManager();
        DiskBlock block = diskManager.allocate();
        diskManager.free(block);
        diskManager.close();
    }
}
