package com.searise.sof.execution;

import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.plan.physics.*;

public class Builder {
    public final Context context;

    public Builder(Context context) {
        this.context = context;
    }

    public Executor build(PhysicalPlan physicalPlan) {
        Executor topExecutor = doBuild(physicalPlan);
        topExecutor = new ResultExec(topExecutor, context);
        return topExecutor;
    }

    private Executor doBuild(PhysicalPlan physicalPlan) {
        String planType = physicalPlan.getClass().getSimpleName();
        switch (planType) {
            case "PhysicalFilter":
                PhysicalFilter filter = (PhysicalFilter) physicalPlan;
                Executor child = doBuild(filter.child);
                return new FilterExec(child, filter.conditions, filter.schema, context);
            case "PhysicalNestedLoopJoin":
                PhysicalNestedLoopJoin nestedLoopJoin = (PhysicalNestedLoopJoin) physicalPlan;
                Executor stream = doBuild(nestedLoopJoin.stream);
                Executor build = doBuild(nestedLoopJoin.build);
                return new NestedLoopJoinExec(stream, build, nestedLoopJoin.conditions, nestedLoopJoin.schema, context);
            case "PhysicalHashJoin":
                PhysicalHashJoin hashJoin = (PhysicalHashJoin) physicalPlan;
                stream = doBuild(hashJoin.stream);
                build = doBuild(hashJoin.build);
                return new HashJoinExec(stream, build, hashJoin.streamJoinKeys, hashJoin.buildJoinKeys, hashJoin.otherConditions, hashJoin.schema, context);
            case "PhysicalProject":
                PhysicalProject project = (PhysicalProject) physicalPlan;
                child = doBuild(project.child);
                return new ProjectExec(child, project.projectList, project.schema, context);
            case "PhysicalScan":
                PhysicalScan scan = (PhysicalScan) physicalPlan;
                return new ScanExec(scan.schema, scan.filePath, scan.separator, context);
            default:
                throw new SofException(String.format("unsupported plan type `%s` in Builder", planType));
        }
    }
}
