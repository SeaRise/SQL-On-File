package com.searise.sof.storge;

import com.google.common.base.Preconditions;
import com.searise.sof.storge.disk.DiskBlock;
import com.searise.sof.storge.disk.DiskManager;
import com.searise.sof.storge.memory.MemoryBlock;
import com.searise.sof.storge.memory.MemoryManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StorageManager implements AutoCloseable {
    private final MemoryManager memoryManager;
    private final DiskManager diskManager;

    private final Set<StorageConsumer> storageConsumers = new HashSet<>();

    public StorageManager() {
        this.memoryManager = new MemoryManager();
        this.diskManager = new DiskManager();
    }

    public void registerConsumer(StorageConsumer consumer) {
        Preconditions.checkArgument(!storageConsumers.contains(consumer));
        storageConsumers.add(consumer);
    }

    public DiskBlock allocateDisk(StorageConsumer consumer) throws IOException {
        Preconditions.checkArgument(!storageConsumers.contains(consumer));
        return diskManager.allocate();
    }

    public Optional<MemoryBlock> allocateMemoryFully(int require, StorageConsumer consumer, boolean shouldAllocated) {
        Preconditions.checkArgument(storageConsumers.contains(consumer));
        return tryAllocateMemory(require, shouldAllocated);
    }

    public Optional<MemoryBlock> allocateMemory(int require, StorageConsumer consumer, boolean shouldAllocated) {
        Preconditions.checkArgument(storageConsumers.contains(consumer));
        Optional<MemoryBlock> tryMemory = tryAllocateMemory(require, shouldAllocated);
        if (tryMemory.isPresent()) {
            return tryMemory;
        }
        return memoryManager.allocate(require, shouldAllocated);
    }

    private Optional<MemoryBlock> tryAllocateMemory(int require, boolean shouldAllocated) {
        Optional<MemoryBlock> tryMemory = memoryManager.allocateFully(require, shouldAllocated);
        if (tryMemory.isPresent()) {
            return tryMemory;
        }

        // 先从占内存大的consumer开始spill.
        List<StorageConsumer> sortByMemoryUsed = storageConsumers.stream().
                sorted((o1, o2) -> Long.compare(o2.memoryUsed(), o1.memoryUsed())).
                collect(Collectors.toList());
        for (StorageConsumer c : sortByMemoryUsed) {
            int missing = require - Math.toIntExact(memoryManager.getFreeSize());
            for (MemoryBlock spilledBlock : c.spill(missing)) {
                memoryManager.free(spilledBlock);
            }

            tryMemory = memoryManager.allocateFully(require, shouldAllocated);
            if (tryMemory.isPresent()) {
                return tryMemory;
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() throws Exception {
        for (StorageConsumer c : storageConsumers) {
            for (Block block : c.getAllBlocksForFree()) {
                if (block instanceof MemoryBlock) {
                    memoryManager.free((MemoryBlock) block);
                } else {
                    diskManager.free((DiskBlock) block);
                }
            }
        }
        memoryManager.close();
        diskManager.close();
    }
}
