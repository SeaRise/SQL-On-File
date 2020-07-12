package com.searise.sof.execution;

import com.searise.sof.core.SofException;
import com.searise.sof.plan.physics.*;

public class Builder {
    public Executor build(PhysicalPlan physicalPlan) {
        Executor topExecutor = doBuild(physicalPlan);
        if (topExecutor.getClass() == ProjectExec.class) {
            topExecutor = new ResultExec(topExecutor);
        }
        return topExecutor;
    }

    private Executor doBuild(PhysicalPlan physicalPlan) {
        String planType = physicalPlan.getClass().getSimpleName();
        switch (planType) {
            case "PhysicalFilter":
                PhysicalFilter filter = (PhysicalFilter) physicalPlan;
                Executor child = doBuild(filter.child);
                return new FilterExec(child, filter.conditions, filter.schema);
            case "PhysicalNestedLoopJoin":
                PhysicalNestedLoopJoin nestedLoopJoin = (PhysicalNestedLoopJoin) physicalPlan;
                Executor stream = doBuild(nestedLoopJoin.stream);
                Executor build = doBuild(nestedLoopJoin.build);
                return new NestedLoopJoinExec(stream, build, nestedLoopJoin.conditions, nestedLoopJoin.schema);
            case "PhysicalProject":
                PhysicalProject project = (PhysicalProject) physicalPlan;
                child = doBuild(project.child);
                return new ProjectExec(child, project.projectList, project.schema);
            case "PhysicalScan":
                PhysicalScan scan = (PhysicalScan) physicalPlan;
                return new ScanExec(scan.schema, scan.filePath, scan.separator);
            default:
                throw new SofException(String.format("unsupported plan type `%s` in Builder", planType));
        }
    }
}
