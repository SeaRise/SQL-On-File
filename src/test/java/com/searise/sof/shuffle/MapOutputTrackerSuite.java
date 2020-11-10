package com.searise.sof.shuffle;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
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
import java.util.concurrent.CountDownLatch;

public class MapOutputTrackerSuite {

    @Test
    public void test() throws InterruptedException {
        MapOutputTracker tracker = new MapOutputTracker();
        long shuffleId = 1;
        int mapPartitions = 20;
        int reducePartitions = 50;

        List<Expression> expressions = expressions();
        List<Expression> keys = ImmutableList.of(expressions.get(1));

        tracker.registerShuffle(shuffleId, mapPartitions);

        Multimap<Integer, String> multiMap = HashMultimap.create();

        CountDownLatch mapLatch = new CountDownLatch(mapPartitions);
        for (int mapId = 0; mapId < mapPartitions; mapId++) {
            int finalMapId = mapId;
            new Thread(() -> {
                ShuffleWriter writer = new ShuffleWriter(keys, shuffleId, finalMapId, tracker, reducePartitions);
                for (int i = 0; i < reducePartitions; i++) {
                    InternalRow row = createRow(finalMapId, i);
                    writer.write(row);

                    int reduceId = ShuffleWriter.hashKey(row, keys, reducePartitions);
                    String key = finalMapId + "_" + reduceId;
                    synchronized (multiMap) {
                        multiMap.put(reduceId, key);
                    }
                }
                writer.commit();
                mapLatch.countDown();
            }).start();
        }
        mapLatch.await();

        Preconditions.checkArgument(multiMap.size() == mapPartitions * reducePartitions);

        CountDownLatch reduceLatch = new CountDownLatch(reducePartitions);
        for (int reduceId = 0; reduceId < reducePartitions; reduceId++) {
            final int finalReduceId = (31 + reduceId) % reducePartitions;
            new Thread(() -> {
                try {
                    ShuffleReader reader = new ShuffleReader(tracker, shuffleId, finalReduceId);
                    Iterator<InternalRow> iterator = reader.iterator();
                    while (iterator.hasNext()) {
                        InternalRow row = iterator.next();
                        int parseReduceId = ShuffleWriter.hashKey(row, keys, reducePartitions);
                        String key = expressions.get(0).eval(row) + "_" + parseReduceId;
                        synchronized (multiMap) {
                            Preconditions.checkArgument(parseReduceId == finalReduceId,
                                    String.format("%s != %s", parseReduceId, finalReduceId));
                            Preconditions.checkArgument(multiMap.get(finalReduceId).contains(key));
                            Preconditions.checkArgument(multiMap.remove(finalReduceId, key));
                        }
                    }
                    reduceLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }).start();
        }
        reduceLatch.await();

        tracker.unregisterShuffle(shuffleId);

        Preconditions.checkArgument(multiMap.isEmpty());
    }

    private InternalRow createRow(int mapId, int reduceId) {
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, mapId);
        writer = InternalRow.getWriter(1, DataType.IntegerType);
        writer.apply(row, reduceId);
        return row;
    }

    private List<Expression> expressions() {
        List<Expression> expressions = new ArrayList<>();
        BoundReference value1 = new BoundReference(DataType.IntegerType, -1);
        value1.resolveIndex(0);
        expressions.add(value1);
        BoundReference value2 = new BoundReference(DataType.IntegerType, -1);
        value2.resolveIndex(1);
        expressions.add(value2);
        return expressions;
    }
}
