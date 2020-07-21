package com.searise.sof.expression.attribute;

import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.Objects;

public class BoundReference implements Expression {
    public final DataType dataType;
    public final long exprId;
    private int index = -1;

    public BoundReference(DataType dataType, long exprId) {
        this.dataType = dataType;
        this.exprId = exprId;
    }

    public BoundReference resolveIndex(int index) {
        if (this.index != -1) {
            throw new SofException("BoundReference has call resolveIndex!");
        }
        this.index = index;
        return this;
    }

    public int index() {
        return index;
    }

    public boolean resolved() {
        return true;
    }

    public String toString() {
        return String.format("%s:exprId->%d:index->%d", dataType, exprId, index);
    }

    public DataType dataType() {
        return dataType;
    }

    public Object eval(InternalRow input) {
        if (index < 0) {
            throw new SofException("can not call eval before calling resolveIndex");
        }
        return InternalRow.getReader(index, dataType).apply(input);
    }

    @Override
    public String genCode() {
        if (index < 0) {
            throw new SofException("can not call genCode before calling resolveIndex");
        }
        String template = "input.get%s(%d)";
        switch (dataType) {
            case StringType:
                return String.format(template, "String", index);
            case BooleanType:
                return String.format(template, "Boolean", index);
            case IntegerType:
                return String.format(template, "Int", index);
            case DoubleType:
                return String.format(template, "Double", index);
            default:
                throw new SofException(String.format("unsupported dataType[%s] in genCode", dataType));
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(exprId);
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || obj.getClass() != BoundReference.class) {
            return false;
        }
        return exprId == ((BoundReference) obj).exprId;
    }
}
