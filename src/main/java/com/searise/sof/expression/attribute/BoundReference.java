package com.searise.sof.expression.attribute;

import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

public class BoundReference implements Expression {
    public final DataType dataType;
    public final long exprId;
    private int index = -1;
    public BoundReference(DataType dataType, long exprId) {
        this.dataType = dataType;
        this.exprId = exprId;
    }

    public BoundReference resolveIndex(int index) {
        this.index = index;
        return this;
    }

    public boolean resolved() {
        return true;
    }

    public String toString() {
        return String.format("%s:%d:%d", dataType, exprId, index);
    }

    public DataType dataType() {
        return dataType;
    }
}
