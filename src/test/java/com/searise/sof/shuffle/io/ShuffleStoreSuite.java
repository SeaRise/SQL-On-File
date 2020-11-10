package com.searise.sof.shuffle.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.type.DataType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShuffleStoreSuite {

    @Test
    public void test() {
        int mapPartitions = 20;
        int reducePartitions = 50;

        List<Expression> keys = keys();

        ShuffleStore shuffleStore = new ShuffleStore();

        Multimap<Integer, String> map = HashMultimap.create();
        for (int mapId = 0; mapId < mapPartitions; mapId++) {
            for (int reduceId = 0; reduceId < reducePartitions; reduceId++) {
                InternalRow row = createRow(mapId, reduceId);
                String value = mapId + "_" + reduceId;
                map.put(reduceId, value);
                shuffleStore.write(reduceId, row);
            }
        }

        for (int reduceId = 0; reduceId < reducePartitions; reduceId++) {
            Preconditions.checkArgument(map.get(reduceId).size() == mapPartitions);
            Iterator<InternalRow> iterator = shuffleStore.read(reduceId);
            while (iterator.hasNext()) {
                InternalRow row = iterator.next();
                int parseReduceId = Integer.valueOf(keys.get(1).eval(row).toString());
                String value = keys.get(0).eval(row) + "_" + parseReduceId;
                Preconditions.checkArgument(map.get(reduceId).contains(value));
                map.remove(reduceId, value);
            }
        }

        Preconditions.checkArgument(map.isEmpty());
    }

    @Test
    public void testHashKey() {

    }

    private InternalRow createRow(int mapId, int reduceId) {
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, mapId);
        writer = InternalRow.getWriter(1, DataType.IntegerType);
        writer.apply(row, reduceId);
        return row;
    }

    private List<Expression> keys() {
        List<Expression> keys = new ArrayList<>();
        BoundReference value1 = new BoundReference(DataType.IntegerType, -1);
        value1.resolveIndex(0);
        keys.add(value1);
        BoundReference value2 = new BoundReference(DataType.IntegerType, -1);
        value2.resolveIndex(1);
        keys.add(value2);
        return keys;
    }
}
