package com.searise.sof.schedule.dag.stage;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Executor;
import com.searise.sof.expression.Expression;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.task.ShuffleMapTask;
import com.searise.sof.schedule.task.Task;
import com.searise.sof.shuffle.MapOutputTracker;
import com.searise.sof.shuffle.io.ShuffleWriter;

import java.util.List;
import java.util.Set;

public class ShuffleMapStage extends Stage {
    public final long shuffleId;
    public final MapOutputTracker mapOutputTracker;
    public final List<Expression> shuffleKeys;
    public final int reduceNum;

    private final ShuffleWriter[] shuffleWriters;

    public ShuffleMapStage(long stageId, Set<Long> parentStageIds, PhysicalPlan plan, long shuffleId, MapOutputTracker mapOutputTracker, List<Expression> shuffleKeys, int reduceNum) {
        super(stageId, parentStageIds, plan);
        this.shuffleId = shuffleId;
        this.mapOutputTracker = mapOutputTracker;
        this.shuffleKeys = shuffleKeys;
        this.reduceNum = reduceNum;
        this.shuffleWriters = new ShuffleWriter[plan.partitions()];
    }

    @Override
    public Task buildTask(Executor executor, int partition) {
        ShuffleWriter shuffleWriter = new ShuffleWriter(shuffleKeys, shuffleId, partition, mapOutputTracker, reduceNum);
        this.shuffleWriters[partition] = shuffleWriter;
        return new ShuffleMapTask(stageId, partition, executor.compute(partition), shuffleWriter);
    }

    @Override
    public List<Integer> getMissPartitions() {
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        for (int partition = 0; partition < partitions; partition++) {
            if (!mapOutputTracker.hasMapOutput(shuffleId, partition)) {
                builder.add(partition);
            }
        }
        return builder.build();
    }

    @Override
    public void success(int partition) {
        checkRange(partition);
        Utils.checkNotNull(shuffleWriters[partition],
                String.format("shuffleWriters[%s] has not init", partition)).commit();
    }
}
