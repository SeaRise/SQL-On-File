package com.searise.sof.expression.attribute;

import com.searise.sof.core.id.ExprIdBuilder;
import com.searise.sof.type.DataType;

import java.util.Objects;
import java.util.Optional;

public class Attribute extends UnresolvedAttribute {
    public final long exprId;
    public final DataType dataType;

    public Attribute(Optional<String> table, String name, long exprId, DataType dataType) {
        super(table, name);
        this.exprId = exprId;
        this.dataType = dataType;
    }

    public boolean resolved() {
        return true;
    }

    public String toString() {
        return String.format("attribute:%s:%s", exprId, dataType);
    }

    public DataType dataType() {
        return dataType;
    }

    public static Attribute newUnknownAttribute(DataType dataType, ExprIdBuilder exprIdBuilder) {
        return new Attribute(Optional.empty(), "", exprIdBuilder.newExprId(), dataType);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(exprId);
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || obj.getClass() != Attribute.class) {
            return false;
        }
        return exprId == ((Attribute) obj).exprId;
    }
}
