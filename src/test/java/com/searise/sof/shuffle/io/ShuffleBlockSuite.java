package com.searise.sof.shuffle.io;

import com.google.common.base.Preconditions;
import com.searise.sof.catalog.TestContext;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.storge.TestConsumer;
import com.searise.sof.type.DataType;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ShuffleBlockSuite {
    @Test
    public void test() {
        try (SofContext context = TestContext.newTestContext()) {
            TestConsumer consumer = new TestConsumer(context.storageManager);
            ShuffleBlock block = new ShuffleBlock(context.storageManager.allocateDisk(consumer));
            int len = 100;
            for (int i = 0; i < len; i++) {
                block.appendDisk(createRow(i));
            }
            AtomicInteger i = new AtomicInteger();
            block.iterator().forEachRemaining(row -> {
                Preconditions.checkArgument(row.getInt(0) == i.get());
                i.incrementAndGet();
            });
            Preconditions.checkArgument(i.get() == len);
        }
    }

    private InternalRow createRow(int index) {
        InternalRow row = new ArrayRow(1);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, index);
        return row;
    }
}
