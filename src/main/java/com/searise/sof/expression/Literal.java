package com.searise.sof.expression;

import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.type.DataType;

public class Literal implements Expression {
    public final DataType dataType;
    public final Object value;

    public Literal(DataType dateType, Object value) {
        this.dataType = dateType;
        this.value = value;
    }

    public String toString() {
        return String.format("literal:%s:%s", value, dataType);
    }

    public boolean resolved() {
        return true;
    }

    public DataType dataType() {
        return dataType;
    }

    public Object eval(InternalRow input) {
        return value;
    }

    public boolean foldable() {
        return true;
    }

    @Override
    public String genCode() {
        if (dataType == DataType.StringType) {
            return String.format("\"%s\"", value.toString());
        }
        return value.toString();
    }
}
