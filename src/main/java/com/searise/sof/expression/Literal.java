package com.searise.sof.expression;

import com.searise.sof.type.DataType;

public class Literal implements Expression {
    public final DataType dataType;
    public final Object value;

    public Literal(DataType dateType, Object value) {
        this.dataType = dateType;
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    public boolean resolved() {
        return true;
    }

    public DataType dataType() {
        return dataType;
    }
}
