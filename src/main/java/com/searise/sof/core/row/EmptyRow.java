package com.searise.sof.core.row;

import com.searise.sof.core.SofException;

public class EmptyRow implements InternalRow {
    public static EmptyRow EMPTY_ROW = new EmptyRow();

    private EmptyRow() {
    }

    @Override
    public int numFields() {
        return 0;
    }

    @Override
    public Object getValue(int ordinal) {
        throw new SofException("EmptyRow do not support getValue");
    }

    @Override
    public void setValue(int ordinal, Object value) {
        throw new SofException("EmptyRow do not support setValue");
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }
}
