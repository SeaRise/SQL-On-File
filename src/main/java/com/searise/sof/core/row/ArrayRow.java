package com.searise.sof.core.row;

import java.util.Arrays;
import java.util.Objects;

public class ArrayRow implements InternalRow {
    private final Object[] array;

    public ArrayRow(int numFields) {
        array = new Object[numFields];
    }

    private ArrayRow(Object[] array) {
        this.array = array;
    }

    @Override
    public InternalRow copy() {
        Object[] copyArray = new Object[array.length];
        System.arraycopy(array, 0, copyArray, 0, array.length);
        return new ArrayRow(copyArray);
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

    @Override
    public String toString() {
        return Arrays.toString(array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (Objects.isNull(other)) {
            return false;
        }

        if (other.getClass() != this.getClass()) {
            return false;
        }

        ArrayRow otherRow = (ArrayRow) other;
        return numFields() == otherRow.numFields() && Arrays.equals(array, otherRow.array);
    }
}
