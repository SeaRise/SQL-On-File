package com.searise.sof.shuffle.io;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.BlockReader;
import com.searise.sof.storge.Block.BlockWriter;
import com.searise.sof.storge.disk.DiskBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShuffleBlock {
    private final List<InternalRow> memoryStore = new ArrayList<>();
    private final DiskBlock diskBlock;
    private BlockWriter diskBlockWriter;

    ShuffleBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
    }

    void appendMemory(InternalRow internalRow) {
        memoryStore.add(internalRow);
    }

    void appendDisk(InternalRow internalRow) {
        Utils.throwRuntime(() -> {
            getDiskBlockWriter().write(internalRow);
            return true;
        });
    }

    private BlockWriter getDiskBlockWriter() {
        return Utils.throwRuntime(() -> {
            if (diskBlockWriter == null) {
                diskBlockWriter = diskBlock.getWriter();
            }
            return diskBlockWriter;
        });
    }

    private void closeDiskBlockWriter() {
        if (diskBlockWriter != null) {
            Utils.throwRuntime(() -> {
                diskBlockWriter.close();
                diskBlockWriter = null;
                return true;
            });
        }
    }

    Iterator<InternalRow> iterator() {
        if (diskBlock.hasUsed()) {
            closeDiskBlockWriter();
            return Utils.throwRuntime(() -> {
                BlockReader blockReader = diskBlock.getReader();
                return Utils.concat(ImmutableList.of(memoryStore.iterator(), new Iterator<InternalRow>() {
                    @Override
                    public boolean hasNext() {
                        return Utils.throwRuntime(() -> {
                            boolean hasNext = blockReader.hasNext();
                            if (!hasNext) {
                                blockReader.close();
                            }
                            return hasNext;
                        });
                    }

                    @Override
                    public InternalRow next() {
                        return Utils.throwRuntime(blockReader::next);
                    }
                }));
            });
        } else {
            return memoryStore.iterator();
        }
    }

    DiskBlock clear() {
        closeDiskBlockWriter();
        memoryStore.clear();
        return diskBlock;
    }
}
