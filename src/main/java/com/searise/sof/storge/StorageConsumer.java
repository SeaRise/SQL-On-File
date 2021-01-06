package com.searise.sof.storge;

import com.searise.sof.storge.memory.MemoryBlock;

import java.util.List;

public abstract class StorageConsumer {
    protected final StorageManager storageManager;
    public StorageConsumer(StorageManager storageManager) {
        this.storageManager = storageManager;
        storageManager.registerConsumer(this);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    public abstract List<MemoryBlock> spill(int require);

    public abstract int memoryUsed();

    public abstract List<Block> getAllBlocksForFree();
}
