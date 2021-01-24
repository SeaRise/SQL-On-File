package com.searise.sof.storge.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.TestContext;
import com.searise.sof.core.SofContext;
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

import java.util.List;

public class BlockSuite {
    @Test
    public void testMemoryBlock() throws Exception {
        try (SofContext context = TestContext.newTestContext()) {
            MemoryAllocator allocator = new MemoryAllocator(context);
            List<InternalRow> rows = createRows(10);
            MemoryBlock block = allocator.allocate(rows.stream().mapToInt(InternalRow::getBytesSize).sum());
            testBlockWriterRead(rows, block);
        }
    }

    @Test
    public void testDiskBlock() throws Exception {
        try (DiskManager diskManager = new DiskManager()) {
            DiskBlock block = diskManager.allocate();
            testBlockWriterRead(createRows(10), block);
        }
    }

    private void testBlockWriterRead(List<InternalRow> rows, Block block) throws Exception {
        try (BlockWriter writer = block.getWriter()) {
            for (InternalRow row : rows) {
                writer.write(row);
            }
        }

        try (BlockReader reader = block.getReader()) {
            for (InternalRow row : rows) {
                Preconditions.checkArgument(row.equals(reader.next()));
            }
        }
    }

    private List<InternalRow> createRows(int length) {
        ImmutableList.Builder<InternalRow> builder = ImmutableList.builder();
        for (int i = 0; i < length; i++) {
            builder.add(createRow());
        }
        return builder.build();
    }

    private InternalRow createRow() {
        InternalRow row = new ArrayRow(4);
        row.setBoolean(0, true);
        row.setInt(1, 1);
        row.setDouble(2, 2.0);
        row.setString(3, "3");
        return row;
    }
}
