package com.searise.sof.execution;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.expr.MutableProjection;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.StorageConsumer;
import com.searise.sof.storge.memory.MemoryBlock;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// 因为HashRelation.spill不生效,
// 而且HashRelation的生命周期只在HashJoinExec内,
// 所以可以不加锁.
public class HashRelation extends StorageConsumer {
    private final Multimap<InternalRow, InternalRow> map;
    private AtomicInteger memoryUsed = new AtomicInteger();
    private final MutableProjection keyProjection;

    HashRelation(SofContext context, MutableProjection keyProjection) {
        super(context.storageManager);
        this.keyProjection = keyProjection;
        this.map = ArrayListMultimap.create();
    }

    public void append(InternalRow row) {
        int require = row.getJVMSize() + keyProjection.defaultSize;
        allocateMemory(require);
        memoryUsed.addAndGet(require);
        map.put(keyProjection.produce(row), row.copy());
    }

    public Iterator<InternalRow> get(InternalRow key) {
        return map.get(key).iterator();
    }

    private void allocateMemory(int require) {
        Utils.checkArgument(
                storageManager.allocateMemoryFully(require, this, false).isPresent(),
                "can't allocate memory for HashRelation");
    }

    @Override
    public List<MemoryBlock> spill(int require) {
        return ImmutableList.of();
    }

    @Override
    public long memoryUsed() {
        return memoryUsed.get();
    }

    @Override
    public synchronized List<Block> getAllBlocksForFree() {
        return ImmutableList.of(MemoryBlock.createNoAllocated(memoryUsed.getAndSet(0)));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
