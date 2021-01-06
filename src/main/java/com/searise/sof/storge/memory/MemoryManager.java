package com.searise.sof.storge.memory;

import java.util.Optional;

public class MemoryManager implements AutoCloseable {
    private final MemoryPool memoryPool;
    private final MemoryAllocator allocator;

    private long overflowSize = 0;

    public MemoryManager() {
        this.memoryPool = new MemoryPool(computeMemoryPoolSize());
        this.allocator = new MemoryAllocator();
    }

    private long computeMemoryPoolSize() {
        return 1;
    }

    public Optional<MemoryBlock> allocateBlockFully(int require) {
        if (!allocateFully(require)) {
            return Optional.empty();
        }
        try {
            return Optional.of(allocator.allocate(require));
        } catch (OutOfMemoryError error) {
            // 说明实际内存分配和内存管理计数不匹配.
            // 内存管理计数算多了,所以多的那一部分就不统计.
            overflowSize += require;
            return allocateBlockFully(require);
        }
    }

    public Optional<MemoryBlock> allocateBlock(int require) {
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
            return allocateBlock(require);
        }
    }

    public boolean allocateFully(int require) {
        int acquired = memoryPool.acquire(require);
        if (acquired < require) {
            memoryPool.release(acquired);
            return false;
        }
        return true;
    }

    public void free(MemoryBlock block) {
        allocator.free(block);
        memoryPool.release(block.allocatedSize);
    }

    public long getFreeSize() {
        return memoryPool.getFreeSize();
    }

    @Override
    public void close() throws Exception {
        memoryPool.release(overflowSize);
        allocator.close();
    }
}
