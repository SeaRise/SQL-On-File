package com.searise.sof.storge.memory;

import com.searise.sof.storge.Block;

import java.util.Optional;

public class MemoryManager {
    private final MemoryPool memoryPool;
    private final MemoryAllocator allocator;

    private int overflowSize = 0;

    public MemoryManager() {
        this.memoryPool = new MemoryPool(computeMemoryPoolSize());
        this.allocator = new MemoryAllocator();
    }

    private long computeMemoryPoolSize() {
        return 1;
    }

    public Optional<Block> allocateFully(int require) {
        int acquired = memoryPool.acquire(require);
        if (acquired < require) {
            memoryPool.release(acquired);
            return Optional.empty();
        }
        try {
            return Optional.of(allocator.allocate(acquired));
        } catch (OutOfMemoryError error) {
            // 说明实际内存分配和内存管理计数不匹配.
            // 内存管理计数算多了,所以多的那一部分就不统计.
            overflowSize += acquired;
            return allocateFully(require);
        }
    }

    public Optional<Block> allocate(int require) {
        int acquired = memoryPool.acquire(require);
        if (acquired <= 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(allocator.allocate(acquired));
        } catch (OutOfMemoryError error) {
            // 说明实际内存分配和内存管理计数不匹配.
            // 内存管理计数算多了,所以多的那一部分就不统计.
            overflowSize += acquired;
            return allocate(require);
        }
    }

    public void free(MemoryBlock block) {
        int length = block.length();
        allocator.free(block);
        memoryPool.release(length);
    }
}
