package com.searise.sof.core.row;

public class ArrayRow implements InternalRow {
    private final Object[] array;

    public ArrayRow(int numFields) {
        array = new Object[numFields];
    }

    @Override
    public int numFields() {
        return array.length;
    }

    @Override
    public Object getValue(int ordinal) {
        rangeCheck(ordinal);
        return array[ordinal];
    }

    @Override
    public void setValue(int ordinal, Object value) {
        rangeCheck(ordinal);
        array[ordinal] = value;
    }
}
