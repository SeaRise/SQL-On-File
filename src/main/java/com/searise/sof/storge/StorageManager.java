package com.searise.sof.storge;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofContext;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.disk.DiskBlock;
import com.searise.sof.storge.disk.DiskManager;
import com.searise.sof.storge.memory.MemoryBlock;
import com.searise.sof.storge.memory.MemoryManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StorageManager implements AutoCloseable {
    private final MemoryManager memoryManager;
    private final DiskManager diskManager;

    private final Set<StorageConsumer> storageConsumers = ConcurrentHashMap.newKeySet();

    public StorageManager(SofContext context) {
        this.memoryManager = new MemoryManager(context);
        this.diskManager = new DiskManager();
    }

    public void registerConsumer(StorageConsumer consumer) {
        Preconditions.checkArgument(!storageConsumers.contains(consumer));
        storageConsumers.add(consumer);
    }

    public void unregisterConsumer(StorageConsumer consumer) {
        Preconditions.checkArgument(storageConsumers.contains(consumer));
        storageConsumers.remove(consumer);
        doFree(consumer);
    }

    public DiskBlock allocateDisk(StorageConsumer consumer) {
        try {
            Preconditions.checkArgument(storageConsumers.contains(consumer));
            return diskManager.allocate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<MemoryBlock> allocateMemoryFully(int require, StorageConsumer consumer, boolean shouldAllocated) {
        Preconditions.checkArgument(storageConsumers.contains(consumer));
        return tryAllocateMemory(require, consumer, shouldAllocated);
    }

    public Optional<MemoryBlock> allocateMemory(int require, StorageConsumer consumer, boolean shouldAllocated) {
        Preconditions.checkArgument(storageConsumers.contains(consumer));
        Optional<MemoryBlock> tryMemory = tryAllocateMemory(require, consumer, shouldAllocated);
        if (tryMemory.isPresent()) {
            return tryMemory;
        }
        return memoryManager.allocate(require, shouldAllocated);
    }

    private Optional<MemoryBlock> tryAllocateMemory(int require, StorageConsumer consumer, boolean shouldAllocated) {
        Optional<MemoryBlock> tryMemory = memoryManager.allocateFully(require, shouldAllocated);
        if (tryMemory.isPresent()) {
            return tryMemory;
        }

        // 先从占内存大的consumer开始spill.
        List<StorageConsumer> sortByMemoryUsed = storageConsumers.stream().
                filter(c -> c != consumer).
                sorted((o1, o2) -> Long.compare(o2.memoryUsed(), o1.memoryUsed())).
                collect(Collectors.toList());
        for (StorageConsumer c : sortByMemoryUsed) {
            spillForRequire(c, require);
            tryMemory = memoryManager.allocateFully(require, shouldAllocated);
            if (tryMemory.isPresent()) {
                return tryMemory;
            }
        }
        spillForRequire(consumer, require);
        return memoryManager.allocateFully(require, shouldAllocated);
    }

    private void spillForRequire(StorageConsumer consumer, int require) {
        int missing = require - Math.toIntExact(memoryManager.getFreeSize());
        for (MemoryBlock spilledBlock : consumer.spill(missing)) {
            memoryManager.free(spilledBlock);
        }
    }

    @Override
    public void close() throws Exception {
        for (StorageConsumer c : storageConsumers) {
            doFree(c);
        }
        storageConsumers.clear();
        memoryManager.close();
        diskManager.close();
    }

    private void doFree(StorageConsumer consumer) {
        for (Block block : consumer.getAllBlocksForFree()) {
            if (block instanceof MemoryBlock) {
                memoryManager.free((MemoryBlock) block);
            } else {
                diskManager.free((DiskBlock) block);
            }
        }
    }
}
