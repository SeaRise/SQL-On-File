package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.exec.Codegen;
import com.searise.sof.codegen.exec.CodegenContext;
import com.searise.sof.codegen.exec.ExecCode;
import com.searise.sof.codegen.exec.ParamGenerator;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.expr.Projection;
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
    private final List<Expression> projectList;
    private final List<BoundReference> schema;
    public final SofContext context;

    public ProjectExec(Executor child, List<Expression> projectList, List<BoundReference> schema, SofContext context) {
        this.context = context;
        Utils.checkArgument(projectList.size() == schema.size(), "projectList.size must equal to schema.size");
        this.child = child;
        this.projectList = projectList;
        this.schema = schema;
    }

    @Override
    public RowIterator compute(int partition) {
        RowIterator childRowIterator = child.compute(partition);
        return new RowIterator() {
            private final Projection projection = new Projection(projectList, new ArrayRow(schema.size()), context);

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

                return projection.apply(input);
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
        return new ProjectExec(child, projectList, schema, context);
    }

    @Override
    public ExecCode genCode(CodegenContext context, ExecCode child) {
        List<ParamGenerator> paramGenerators = combine(new ParamGenerator() {
            private final String name = context.genVar(variablePrefix, "projection");

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
                return new Projection(projectList, new ArrayRow(schema.size()), ProjectExec.this.context);
            }
        }, child.paramGenerators);

        String input = child.output;
        String output = context.genVar(variablePrefix, "output");
        String codeTemplate =
                "if (%s == EMPTY_ROW) {\n" +
                        "    return EMPTY_ROW;\n" +
                        "}\n" +
                        "\n" +
                        "%s = projection.apply(%s)\n";
        String code = String.format(codeTemplate, input, output, input);

        return new ExecCode(code, output, paramGenerators, child.importClasses);
    }

    private static <T> List<T> combine(T t1, List<T> t2) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        builder.add(t1);
        builder.addAll(t2);
        return builder.build();
    }
}
