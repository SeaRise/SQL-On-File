package com.searise.sof.shuffle.io;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.disk.DiskBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShuffleBlock {
    private final List<InternalRow> memoryBlock = new ArrayList<>();
    private final DiskBlock diskBlock;

    ShuffleBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
    }

    void appendMemory(InternalRow internalRow) {
        memoryBlock.add(internalRow);
    }

    void appendDisk(InternalRow internalRow) {
        throw new UnsupportedOperationException();
    }

    Iterator<InternalRow> iterator() {
        return memoryBlock.iterator();
    }

    DiskBlock clear() {
        memoryBlock.clear();
        return diskBlock;
    }
}
