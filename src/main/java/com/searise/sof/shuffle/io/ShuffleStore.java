package com.searise.sof.shuffle.io;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.StorageConsumer;
import com.searise.sof.storge.memory.MemoryBlock;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ShuffleStore extends StorageConsumer {
    private final ShuffleBlock[] shuffleBlocks;
    private AtomicInteger memoryUsed = new AtomicInteger();

    public ShuffleStore(int partitions) {
        super(SofContext.getActive().storageManager);
        this.shuffleBlocks = new ShuffleBlock[partitions];
        for (int i = 0; i < shuffleBlocks.length; i++) {
            shuffleBlocks[i] = new ShuffleBlock(storageManager.allocateDisk(this));
        }
    }

    public void write(int partition, InternalRow internalRow) {
        ShuffleBlock shuffleBlock = shuffleBlocks[partition];
        synchronized (shuffleBlock) {
            int memoryRequire = internalRow.getJVMSize();
            if (storageManager.allocateMemoryFully(memoryRequire, this, false).isPresent()) {
                memoryUsed.addAndGet(memoryRequire);
                // row需要copy, 因为保存的internalRow可能还有其他地方修改.
                shuffleBlock.appendMemory(internalRow.copy(), memoryRequire);
            } else {
                shuffleBlock.appendDisk(internalRow);
            }
        }
    }

    public Iterator<InternalRow> read(int partition) {
        ShuffleBlock shuffleBlock = shuffleBlocks[partition];
        synchronized (shuffleBlock) {
            return shuffleBlock.iterator();
        }
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(shuffleBlocks);
    }

    @Override
    public List<MemoryBlock> spill(int require) {
//        int spillMemorySize = Stream.of(shuffleBlocks).mapToInt(block -> {
//            synchronized (block) {
//                return block.spill();
//            }
//        }).sum();
//        if (spillMemorySize > 0) {
//            memoryUsed.getAndAdd(-spillMemorySize);
//            return ImmutableList.of(MemoryBlock.createNoAllocated(spillMemorySize));
//        } else {
//            return ImmutableList.of();
//        }
        return ImmutableList.of();
    }

    @Override
    public long memoryUsed() {
        return memoryUsed.get();
    }

    @Override
    public List<Block> getAllBlocksForFree() {
        ImmutableList.Builder<Block> blockBuilder = ImmutableList.builder();
        for (ShuffleBlock shuffleBlock : shuffleBlocks) {
            synchronized (shuffleBlock) {
                blockBuilder.add(shuffleBlock.clear());
            }
        }
        blockBuilder.add(MemoryBlock.createNoAllocated(memoryUsed.getAndSet(0)));
        return blockBuilder.build();
    }
}
