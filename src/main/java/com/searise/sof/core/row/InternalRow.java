package com.searise.sof.core.row;

import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.type.DataType;

public interface InternalRow {
    int numFields();

    Object getValue(int ordinal);

    default InternalRow copy() {
        throw new UnsupportedOperationException();
    }

    default String getString(int ordinal) {
        return (String) getValue(ordinal);
    }

    default boolean getBoolean(int ordinal) {
        return (boolean) getValue(ordinal);
    }

    default int getInt(int ordinal) {
        return (int) getValue(ordinal);
    }

    default double getDouble(int ordinal) {
        return (double) getValue(ordinal);
    }

    default long getSize() {
        long size = 0L;
        for (int i = 0; i < numFields(); i++) {
            size += Utils.getSize(getValue(i));
        }
        return size;
    }

    void setValue(int ordinal, Object value);

    default void setString(int ordinal, String value) {
        setValue(ordinal, value);
    }

    default void setBoolean(int ordinal, boolean value) {
        setValue(ordinal, value);
    }

    default void setInt(int ordinal, int value) {
        setValue(ordinal, value);
    }

    default void setDouble(int ordinal, double value) {
        setValue(ordinal, value);
    }

    default void rangeCheck(int index) {
        Utils.checkArgument(index >= 0 && index < numFields(),
                "out of bounds Index: " + index + ", Size: " + numFields());
    }

    static InternalRowWriter getWriter(int ordinal, DataType dataType) {
        switch (dataType) {
            case StringType:
                return (row, value) -> row.setString(ordinal, value.toString());
            case BooleanType:
                return (row, value) -> row.setBoolean(ordinal, (boolean) value);
            case IntegerType:
                return (row, value) -> row.setInt(ordinal, (int) value);
            case DoubleType:
                return (row, value) -> row.setDouble(ordinal, (double) value);
            default:
                throw new SofException(String.format("unsupported dataType[%s] in getWriter", dataType));
        }
    }

    static InternalRowReader getReader(int ordinal, DataType dataType) {
        Utils.checkArgument(ordinal >= 0, "ordinal must >= 0");
        switch (dataType) {
            case StringType:
                return (row) -> row.getString(ordinal);
            case BooleanType:
                return (row) -> row.getBoolean(ordinal);
            case IntegerType:
                return (row) -> row.getInt(ordinal);
            case DoubleType:
                return (row) -> row.getDouble(ordinal);
            default:
                throw new SofException(String.format("unsupported dataType[%s] in getReader", dataType));
        }
    }
}
