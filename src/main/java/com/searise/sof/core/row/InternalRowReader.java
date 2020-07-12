package com.searise.sof.core.row;

@FunctionalInterface
public interface InternalRowReader {
    Object apply(InternalRow row);
}
