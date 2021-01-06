package com.searise.sof.storge.memory;

import com.searise.sof.storge.Block;

import java.nio.ByteBuffer;

public class MemoryBlock implements Block {
    ByteBuffer byteBuffer;
    private int length;

    public MemoryBlock(ByteBuffer byteBuffer, int length) {
        this.byteBuffer = byteBuffer;
        this.length = length;
    }

    void free() {
        // just gc
        this.byteBuffer = null;
        this.length = 0;
    }

    @Override
    public int length() {
        return length;
    }
}
