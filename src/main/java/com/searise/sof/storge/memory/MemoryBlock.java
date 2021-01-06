package com.searise.sof.storge.memory;

import com.searise.sof.storge.Block;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MemoryBlock implements Block {
    ByteBuffer byteBuffer;
    final int allocatedSize;
    // 是否有实际分配内存.
    private final boolean isAllocated;

    MemoryBlock(ByteBuffer byteBuffer, int allocatedSize) {
        this(byteBuffer, allocatedSize, true);
    }

    private MemoryBlock(ByteBuffer byteBuffer, int allocatedSize, boolean isAllocated) {
        this.byteBuffer = byteBuffer;
        this.allocatedSize = allocatedSize;
        this.isAllocated =  isAllocated;
    }

    @Override
    public int capacity() {
        return Objects.isNull(byteBuffer) ? 0 : byteBuffer.capacity();
    }

    @Override
    public void free() {
        // just gc
        this.byteBuffer = null;
    }

    public boolean isAllocated() {
        return isAllocated;
    }

    // 用于创建没有实际分配内存的MemoryBlock.
    // 用于释放分配时没有走allocator的内存.
    public static MemoryBlock createNoAllocated(int require) {
        return new MemoryBlock(null, require, false);
    }
}
