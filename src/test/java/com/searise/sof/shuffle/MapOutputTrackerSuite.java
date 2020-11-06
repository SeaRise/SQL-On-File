package com.searise.sof.shuffle;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.shuffle.io.ShuffleReader;
import com.searise.sof.shuffle.io.ShuffleWriter;
import com.searise.sof.type.DataType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

public class MapOutputTrackerSuite {

    @Test
    public void test() throws InterruptedException {
        MapOutputTracker tracker = new MapOutputTracker();
        long shuffleId = 0;
        int mapPartitions = 500;
        int reducePartitions = 300;

        List<Expression> keys = keys();

        tracker.registerShuffle(shuffleId, mapPartitions);

        Set<String> set = new ConcurrentSkipListSet<>();

        CountDownLatch mapLatch = new CountDownLatch(mapPartitions);
        for (int mapId = 0; mapId < mapPartitions; mapId++) {
            int finalMapId = mapId;
            new Thread(() -> {
                ShuffleWriter writer = new ShuffleWriter(keys, shuffleId, finalMapId, tracker, reducePartitions);
                for (int i = 0; i < 5; i++) {
                    writer.write(createRow(finalMapId, i));
                    String key = finalMapId + "_" + i;
                    set.add(key);
                }
                writer.commit();
                mapLatch.countDown();
            }).start();
        }
        mapLatch.await();

        Preconditions.checkArgument(set.size() == mapPartitions * 5);

        CountDownLatch reduceLatch = new CountDownLatch(reducePartitions);
        for (int reduceId = 0; reduceId < reducePartitions; reduceId++) {
            final int finalReduceId = reduceId;
            new Thread(() -> {
                ShuffleReader reader = new ShuffleReader(tracker, shuffleId, finalReduceId);
                Iterator<InternalRow> iterator = reader.iterator();
                while (iterator.hasNext()) {
                    InternalRow row = iterator.next();
                    String key = keys.get(0).eval(row) + "_" + keys.get(1).eval(row);
                    Preconditions.checkArgument(set.contains(key));
                    set.remove(key);
                }
                reduceLatch.countDown();
            }).start();
        }
        reduceLatch.await();

        tracker.unregisterShuffle(shuffleId);

        Preconditions.checkArgument(set.isEmpty());
    }

    private InternalRow createRow(int partition, int index) {
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, partition);
        writer = InternalRow.getWriter(1, DataType.IntegerType);
        writer.apply(row, index);
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
