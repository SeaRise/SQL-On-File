package com.searise.sof.storge;

import com.google.common.collect.ImmutableList;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.memory.MemoryBlock;

import java.util.List;

public class TestConsumer extends StorageConsumer {
    public TestConsumer(StorageManager storageManager) {
        super(storageManager);
    }

    @Override
    public List<MemoryBlock> spill(int require) {
        return ImmutableList.of();
    }

    @Override
    public long memoryUsed() {
        return 0;
    }

    @Override
    public List<Block> getAllBlocksForFree() {
        return ImmutableList.of();
    }
}
