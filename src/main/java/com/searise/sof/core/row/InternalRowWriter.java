package com.searise.sof.core.row;

@FunctionalInterface
public interface InternalRowWriter {
    void apply(InternalRow row, Object value);
}
