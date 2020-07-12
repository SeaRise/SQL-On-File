package com.searise.sof.execution;

import com.searise.sof.core.Projection;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class ProjectExec implements Executor {

    private final Executor child;
    private final Projection projection;
    public ProjectExec(Executor child, List<Expression> projectList, List<BoundReference> schema) {
        Utils.checkArgument(projectList.size() == schema.size(), "projectList.size must equal to schema.size");
        this.child = child;
        InternalRow output = new ArrayRow(schema.size());
        projection = new Projection(projectList, output);
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
}
