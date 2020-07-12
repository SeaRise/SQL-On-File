package com.searise.sof.core;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowReader;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.type.DataType;
import org.junit.Test;

public class InternalRowSuite {

    @Test
    public void test() {
        InternalRow row = new ArrayRow(4);
        writeAndRead(row, 0, "test", DataType.StringType);
        writeAndRead(row, 1, 1, DataType.IntegerType);
        writeAndRead(row, 2, 0.1, DataType.DoubleType);
        writeAndRead(row, 3, true, DataType.BooleanType);
    }

    private void writeAndRead(InternalRow row, int index, Object value, DataType dataType) {
        InternalRowWriter writer = InternalRow.getWriter(index, dataType);
        writer.apply(row, value);

        InternalRowReader reader = InternalRow.getReader(index, dataType);
        Object readValue = reader.apply(row);

        Preconditions.checkArgument(value.equals(readValue));
    }
}
