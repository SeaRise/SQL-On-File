package com.searise.sof.execution;

import com.searise.sof.codegen.exec.CodeGenerator;
import com.searise.sof.codegen.exec.Codegen;
import com.searise.sof.codegen.exec.CodegenContext;
import com.searise.sof.codegen.exec.ExecCode;
import com.searise.sof.core.Conf;
import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.plan.physics.*;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;

import java.io.IOException;
import java.util.stream.Collectors;

public class Builder {
    public final Context context;

    public Builder(Context context) {
        this.context = context;
    }

    public Executor build(PhysicalPlan physicalPlan) {
        return codegen(doBuild(physicalPlan));
    }

    private Executor codegen(Executor executor) {
        if (context.conf.getBoolConf(Conf.CODEGEN_EXECUTOR)) {
            Pair<ExecCode, Executor> codegenResult = doCodegen(new CodegenContext(), executor);
            try {
                return CodeGenerator.gen(codegenResult.getLeft(), codegenResult.getRight());
            } catch (Exception e) {
                return executor;
            }
        }

        return executor;
    }

    private Pair<ExecCode, Executor> doCodegen(CodegenContext context, Executor executor) {
        if (executor instanceof Codegen) {
            Codegen codegen = (Codegen) executor;
            Pair<ExecCode, Executor> childCodegenResult = doCodegen(context, codegen.child());
            return Pair.of(codegen.genCode(context, childCodegenResult.getLeft()), childCodegenResult.getRight());
        }

        Executor newExecutor = executor.copyWithNewChildren(Utils.toImmutableList(executor.children().stream().map(this::codegen)));
        return Pair.of(ExecCode.INIT_EXPR_CODE, newExecutor);
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
