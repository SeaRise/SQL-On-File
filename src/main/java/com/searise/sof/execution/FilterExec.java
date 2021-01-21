package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.exec.Codegen;
import com.searise.sof.codegen.exec.CodegenContext;
import com.searise.sof.codegen.exec.ExecCode;
import com.searise.sof.codegen.exec.ParamGenerator;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.expr.Predication;
import com.searise.sof.core.expr.Projection;
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
    private final List<Expression> conditions;
    private final List<BoundReference> schema;
    public final SofContext context;

    public FilterExec(Executor child, List<Expression> conditions, List<BoundReference> schema, SofContext context) {
        this.child = child;
        this.context = context;
        this.conditions = conditions;
        this.schema = schema;
    }

    @Override
    public RowIterator compute(int partition) {
        RowIterator childRowIterator = child.compute(partition);
        return new RowIterator() {
            private final Projection schemaProjection = new Projection(
                    Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)),
                    new ArrayRow(schema.size()), context);
            private final Predication predication = new Predication(conditions, context);

            @Override
            public void open() {
                childRowIterator.open();
            }

            @Override
            public boolean hasNext() {
                return childRowIterator.hasNext();
            }

            @Override
            public InternalRow next() {
                InternalRow input = childRowIterator.next();
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
                childRowIterator.close();
            }
        };
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of(child);
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new FilterExec(child, conditions, schema, context);
    }

    @Override
    public ExecCode genCode(CodegenContext context, ExecCode child) {
        List<ParamGenerator> paramGenerators = combine(new ParamGenerator() {
            private final String name = context.genVar(variablePrefix, "schemaProjection");

            @Override
            public String name() {
                return name;
            }

            @Override
            public Class clazz() {
                return Projection.class;
            }

            @Override
            public Object gen() {
                return new Projection(
                        Utils.toImmutableList(schema.stream().map(boundReference -> (Expression) boundReference)),
                        new ArrayRow(schema.size()), FilterExec.this.context);
            }
        }, new ParamGenerator() {
            private final String name = context.genVar(variablePrefix, "predication");

            @Override
            public String name() {
                return name;
            }

            @Override
            public Class clazz() {
                return Predication.class;
            }

            @Override
            public Object gen() {
                return new Predication(conditions, FilterExec.this.context);
            }
        }, child.paramGenerators);

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

        return new ExecCode(code, output, paramGenerators, child.importClasses);
    }

    private static <T> List<T> combine(T t1, T t2, List<T> t3) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.add(t1);
        builder.add(t2);
        builder.addAll(t3);
        return builder.build();
    }
}
