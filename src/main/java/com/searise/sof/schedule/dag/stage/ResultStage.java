package com.searise.sof.schedule.dag.stage;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Executor;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.ResultHandle;
import com.searise.sof.schedule.task.ResultTask;
import com.searise.sof.schedule.task.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultStage extends Stage {
    public final ResultHandle resultHandle;
    private final Set<Integer> missPartitions;

    public ResultStage(long stageId, Set<Long> parentStageIds, PhysicalPlan plan, ResultHandle resultHandle) {
        super(stageId, parentStageIds, plan);
        this.resultHandle = resultHandle;
        missPartitions = new HashSet<>();
        for (int partition = 0; partition < partitions; partition++) {
            missPartitions.add(partition);
        }
    }

    @Override
    public Task buildTask(Executor executor, int partition) {
        return new ResultTask(stageId, partition, executor.compute(partition), resultHandle);
    }

    @Override
    public List<Integer> getMissPartitions() {
        return ImmutableList.copyOf(missPartitions);
    }

    @Override
    public void success(int partition) {
        checkRange(partition);
        Utils.checkArgument(missPartitions.remove(partition),
                String.format("partition(%s) has success", partition));
    }
}
