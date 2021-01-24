package com.searise.sof.storge.memory;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;

public class MemoryPool {
    private final long poolSize;
    private long memoryUsed = 0L;

    MemoryPool(long poolSize) {
        Preconditions.checkArgument(poolSize > 0);
        this.poolSize = poolSize;
    }

    public int acquire(int require) {
        Preconditions.checkArgument(require >= 0);
        if (require == 0) {
            return 0;
        }

        long free = getFreeSize();
        if (free <= require) {
            memoryUsed = poolSize;
            return Math.toIntExact(free);
        }
        memoryUsed += require;
        return require;
    }

    public void release(long used) {
        Utils.checkArgument(used >= 0 && used <= memoryUsed,
                String.format("release memory(%s) < 0 or > memoryUsed(%s)", used, memoryUsed));
        memoryUsed -= used;
    }

    public long getPoolSize() {
        return poolSize;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public long getFreeSize() {
        return poolSize - memoryUsed;
    }
}
