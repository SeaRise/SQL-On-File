package com.searise.sof.expression;

import com.searise.sof.type.DataType;

public class Attribute extends UnresolvedAttribute {
    public final long exprId;
    public final DataType dataType;
    public Attribute(String table, String name, long exprId, DataType dataType) {
        super(table, name);
        this.exprId = exprId;
        this.dataType = dataType;
    }

    public boolean resolved() {
        return true;
    }

    public String toString() {
        return String.format("%s:%s", exprId, dataType);
    }
}
