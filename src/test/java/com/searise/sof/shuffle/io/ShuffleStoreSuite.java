package com.searise.sof.shuffle.io;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowReader;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.type.DataType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;

public class ShuffleStoreSuite {

    @Test
    public void test() {
        ShuffleStore shuffleStore = new ShuffleStore();

        HashMap<Integer, InternalRow> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            InternalRow row1 = createRow(i);
            map.put(i, row1);
            shuffleStore.write(i, row1);

            InternalRow row2 = createRow(100+i);
            map.put(100+i, row2);
            shuffleStore.write(i, row2);
        }

        for (int i = 0; i < 100; i++) {
            Iterator<InternalRow> iterator = shuffleStore.read(i);
            while (iterator.hasNext()) {
                InternalRow row = iterator.next();
                InternalRowReader reader = InternalRow.getReader(0, DataType.IntegerType);
                int index = (Integer) reader.apply(row);
                Preconditions.checkArgument(map.containsKey(index));
                map.remove(index);
            }
        }

        Preconditions.checkArgument(map.isEmpty());
    }

    private InternalRow createRow(int index) {
        BoundReference value = new BoundReference(DataType.IntegerType, -1);
        value.resolveIndex(0);
        InternalRow row = new ArrayRow(1);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, index);
        return row;
    }
}
