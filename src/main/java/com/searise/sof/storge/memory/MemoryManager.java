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

    private static long RESERVED_SYSTEM_MEMORY_BYTES = 300 * 1024 * 1024;
    private static double MEMORY_FRACTION = 0.6;
    private long computeMemoryPoolSize() {
        long systemMemory = Runtime.getRuntime().maxMemory();
        long minSystemMemory = (long) (RESERVED_SYSTEM_MEMORY_BYTES * 1.5);
        if (systemMemory < minSystemMemory) {
            throw new IllegalArgumentException(
                    String.format("System memory %s must be at least %s.",
                    systemMemory, minSystemMemory));
        }
        return (long) ((systemMemory - RESERVED_SYSTEM_MEMORY_BYTES) * MEMORY_FRACTION);
    }

    public Optional<MemoryBlock> allocateFully(int require, boolean shouldAllocated) {
        int acquired = memoryPool.acquire(require);
        if (acquired < require) {
            memoryPool.release(acquired);
            return Optional.empty();
        }
        try {
            return Optional.of(shouldAllocated ?
                    allocator.allocate(require) :
                    MemoryBlock.createNoAllocated(require));
        } catch (OutOfMemoryError error) {
            // 说明实际内存分配和内存管理计数不匹配.
            // 内存管理计数算多了,所以多的那一部分就不统计.
            overflowSize += require;
            return allocateFully(require, shouldAllocated);
        }
    }

    public Optional<MemoryBlock> allocate(int require, boolean shouldAllocated) {
        int acquired = memoryPool.acquire(require);
        if (acquired <= 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(shouldAllocated ?
                    allocator.allocate(require) :
                    MemoryBlock.createNoAllocated(require));
        } catch (OutOfMemoryError error) {
            // 说明实际内存分配和内存管理计数不匹配.
            // 内存管理计数算多了,所以多的那一部分就不统计.
            overflowSize += acquired;
            return allocate(require, shouldAllocated);
        }
    }

    public void free(MemoryBlock block) {
        memoryPool.release(block.allocatedSize);
        if (block.isAllocated()) {
            allocator.free(block);
        }
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
