package com.searise.sof.storge.memory;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.nio.ByteBuffer;

public class MemoryAllocatorSuite {
    @Test
    public void test() {
        MemoryAllocator allocator = new MemoryAllocator();
        MemoryBlock block = allocator.allocate(1);
        ByteBuffer buffer = block.byteBuffer;
        allocator.free(block);
        Preconditions.checkArgument(block.byteBuffer == null);
        block = allocator.allocate(1);
        // 小block不会缓存复用.
        Preconditions.checkArgument(block.byteBuffer != buffer);
        allocator.free(block);
        Preconditions.checkArgument(block.byteBuffer == null);

        block = allocator.allocate(5 * 1024 * 1024);
        buffer = block.byteBuffer;
        allocator.free(block);
        Preconditions.checkArgument(block.byteBuffer == null);
        block = allocator.allocate(5 * 1024 * 1024);
        // 大block缓存复用
        Preconditions.checkArgument(block.byteBuffer == buffer);
        allocator.free(block);
        Preconditions.checkArgument(block.byteBuffer == null);
    }
}
