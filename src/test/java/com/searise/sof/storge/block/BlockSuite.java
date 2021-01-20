package com.searise.sof.storge.block;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.Block.BlockReader;
import com.searise.sof.storge.Block.BlockWriter;
import com.searise.sof.storge.disk.DiskBlock;
import com.searise.sof.storge.disk.DiskManager;
import com.searise.sof.storge.memory.MemoryAllocator;
import com.searise.sof.storge.memory.MemoryBlock;
import org.junit.Test;

public class BlockSuite {
    @Test
    public void testMemoryBlock() throws Exception {
        InternalRow row = new ArrayRow(4);
        row.setBoolean(0, true);
        row.setInt(1, 1);
        row.setDouble(2, 2.0);
        row.setString(3, "3");

        MemoryAllocator allocator = new MemoryAllocator();
        MemoryBlock block = allocator.allocate(row.getBytesSize());
        testBlockWriterRead(row, block);
    }

    @Test
    public void testDiskBlock() throws Exception {
        InternalRow row = new ArrayRow(4);
        row.setBoolean(0, true);
        row.setInt(1, 1);
        row.setDouble(2, 2.0);
        row.setString(3, "3");

        try (DiskManager diskManager = new DiskManager()) {
            DiskBlock block = diskManager.allocate();
            testBlockWriterRead(row, block);
        }
    }

    private void testBlockWriterRead(InternalRow row, Block block) throws Exception {
        BlockWriter writer = block.getWriter();
        writer.write(row);
        writer.close();

        BlockReader reader = block.getReader();
        Preconditions.checkArgument(reader.hasNext());
        Preconditions.checkArgument(row.equals(reader.next()));
    }
}
