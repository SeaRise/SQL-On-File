package com.searise.sof.storge.memory;

import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MemoryAllocator implements AutoCloseable {
    private Map<Integer, LinkedList<WeakReference<ByteBuffer>>> bufferPoolsBySize = new HashMap<>();

    private static final int POOLING_THRESHOLD_BYTES = 1024 * 1024; // 1MB

    public MemoryBlock allocate(int require) {
        int alignedSize = alignedSize(require);
        if (alignedSize >= POOLING_THRESHOLD_BYTES) {
            final LinkedList<WeakReference<ByteBuffer>> pool = bufferPoolsBySize.get(alignedSize);
            if (pool != null) {
                while (!pool.isEmpty()) {
                    final WeakReference<ByteBuffer> arrayReference = pool.pop();
                    final ByteBuffer byteBuffer = arrayReference.get();
                    if (byteBuffer != null) {
                        // 如果之前有缓存好的内存块,而且正好又没有被gc,那就可以返回.
                        return new MemoryBlock(byteBuffer, require);
                    }
                }
            }
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(alignedSize);
        return new MemoryBlock(byteBuffer, require);
    }

    public void free(MemoryBlock block) {
        int alignedSize = alignedSize(block.allocatedSize);
        if (alignedSize >= POOLING_THRESHOLD_BYTES) {
            final LinkedList<WeakReference<ByteBuffer>> pool =
                    bufferPoolsBySize.getOrDefault(alignedSize, new LinkedList<>());
            pool.add(new WeakReference<>(block.byteBuffer));
            bufferPoolsBySize.put(alignedSize, pool);
        }
        block.free();
    }

    // 一般来讲,单次分配的误差最多是7,
    // memoryManager会预留一些堆内存的.
    // 为了内存规整,这点损耗值得.
    private int alignedSize(int size) {
        int numWords = ((size + 7) / 8);
        int alignedSize = numWords * 8;
        Preconditions.checkArgument(alignedSize >= size);
        return alignedSize;
    }

    @Override
    public void close() throws Exception {
        bufferPoolsBySize = null;
    }
}
