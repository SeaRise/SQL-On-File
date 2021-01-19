package com.searise.sof.storge.memory;

import com.google.common.base.Preconditions;
import org.junit.Test;

public class MemoryPoolSuite {
    @Test
    public void test() {
        MemoryPool pool = new MemoryPool(10);
        Preconditions.checkArgument(pool.acquire(5) == 5);
        Preconditions.checkArgument(pool.getFreeSize() == 5);
        Preconditions.checkArgument(pool.acquire(5) == 5);
        Preconditions.checkArgument(pool.getFreeSize() == 0);
        pool.release(5);
        Preconditions.checkArgument(pool.getFreeSize() == 5);
        Preconditions.checkArgument(pool.acquire(6) == 5);
        Preconditions.checkArgument(pool.getFreeSize() == 0);
        Preconditions.checkArgument(pool.acquire(1) == 0);
    }
}
