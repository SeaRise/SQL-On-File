package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.exec.Codegen;
import com.searise.sof.codegen.exec.CodegenContext;
import com.searise.sof.codegen.exec.ExecCode;
import com.searise.sof.core.Context;
import com.searise.sof.core.Predication;
import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class FilterExec extends Codegen implements Executor {

    private final Executor child;
    private final Projection schemaProjection;
    private final Predication predication;
    public final Context context;

    public FilterExec(Executor child, List<Expression> conditions, List<BoundReference> schema, Context context) {
        this.child = child;
        this.context = context;
        InternalRow output = new ArrayRow(schema.size());
        this.schemaProjection = new Projection(Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)), output, context);
        this.predication = new Predication(conditions, context);
    }

    private FilterExec(Executor child, Projection schemaProjection, Predication predication, Context context) {
        this.child = child;
        this.schemaProjection = schemaProjection;
        this.predication = predication;
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

        if (!predication.apply(input)) {
            return EMPTY_ROW;
        }

        return schemaProjection.apply(input);
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
        return new FilterExec(child, schemaProjection, predication, context);
    }

    @Override
    public ExecCode genCode(CodegenContext context, ExecCode child) {
        List<Object> params = combine(schemaProjection, predication, child.params);
        List<String> paramNames = combine(
                context.genVar(variablePrefix, "schemaProjection"),
                context.genVar(variablePrefix, "predication"),
                child.paramNames);
        List<Class> paramClasses = combine(Projection.class, Predication.class, child.paramClasses);

        String input = child.output;
        String output = context.genVar(variablePrefix, "output");
        String codeTemplate =
                "if (%s == EMPTY_ROW) {\n" +
                "    return EMPTY_ROW;\n" +
                "}\n" +
                "\n" +
                "if (!predication.apply(%s)) {\n" +
                "    return EMPTY_ROW;\n" +
                "}" +
                "\n" +
                "%s = schemaProjection.apply(%s)\n";
        String code = String.format(codeTemplate, input, input, output, input);

        return new ExecCode(code, output, params, paramNames, paramClasses, child.paramClasses);
    }

    private static <T> List<T> combine(T t1, T t2, List<T> t3) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.add(t1);
        builder.add(t2);
        builder.addAll(t3);
        return builder.build();
    }
}
