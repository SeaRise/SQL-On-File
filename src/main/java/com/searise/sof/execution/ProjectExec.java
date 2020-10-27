package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.exec.Codegen;
import com.searise.sof.codegen.exec.CodegenContext;
import com.searise.sof.codegen.exec.ExecCode;
import com.searise.sof.core.Context;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ProjectExec extends Codegen implements Executor {

    private final Executor child;
    private final Projection projection;
    public final Context context;

    public ProjectExec(Executor child, List<Expression> projectList, List<BoundReference> schema, Context context) {
        this.context = context;
        Utils.checkArgument(projectList.size() == schema.size(), "projectList.size must equal to schema.size");
        this.child = child;
        InternalRow output = new ArrayRow(schema.size());
        projection = new Projection(projectList, output, context);
    }

    private ProjectExec(Executor child, Projection projection, Context context) {
        this.child = child;
        this.projection = projection;
        this.context = context;
    }

    @Override
    public void open() {
        child.open();
    }

    @Override
    public boolean hasNext() {
        return child.hasNext();
    }

    @Override
    public InternalRow next() {
        InternalRow input = child.next();
        if (input == EMPTY_ROW) {
            return EMPTY_ROW;
        }

        return projection.apply(input);
    }

    @Override
    public void close() {
        child.close();
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of(child);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new ProjectExec(child, projection, context);
    }

    @Override
    public ExecCode genCode(CodegenContext context, ExecCode child) {
        List<Object> params = combine(projection, child.params);
        List<String> paramNames = combine(context.genVar(variablePrefix, "projection"), child.paramNames);
        List<Class> paramClasses = combine(Projection.class, child.paramClasses);

        String input = child.output;
        String output = context.genVar(variablePrefix, "output");
        String codeTemplate =
                "if (%s == EMPTY_ROW) {\n" +
                "    return EMPTY_ROW;\n" +
                "}\n" +
                "\n" +
                "%s = projection.apply(%s)\n";
        String code = String.format(codeTemplate, input, output, input);

        return new ExecCode(code, output, params, paramNames, paramClasses, child.paramClasses);
    }

    private static <T> List<T> combine(T t1, List<T> t2) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.add(t1);
        builder.addAll(t2);
        return builder.build();
    }
}
