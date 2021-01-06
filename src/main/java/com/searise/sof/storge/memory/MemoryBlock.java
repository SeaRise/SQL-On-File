package com.searise.sof.storge.memory;

import com.searise.sof.storge.Block;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MemoryBlock implements Block {
    ByteBuffer byteBuffer;
    final int allocatedSize;

    MemoryBlock(ByteBuffer byteBuffer, int allocatedSize) {
        this.byteBuffer = byteBuffer;
        this.allocatedSize = allocatedSize;
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
}
