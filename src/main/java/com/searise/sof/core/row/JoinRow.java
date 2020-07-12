package com.searise.sof.core.row;

import com.searise.sof.core.SofException;

public class JoinRow implements InternalRow {
    public final InternalRow left;
    public final InternalRow right;

    public JoinRow(InternalRow left, InternalRow right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int numFields() {
        return left.numFields() + right.numFields();
    }

    @Override
    public Object getValue(int ordinal) {
        rangeCheck(ordinal);
        if (ordinal < left.numFields()) {
            return left.getValue(ordinal);
        } else {
            return right.getValue(ordinal - left.numFields());
        }
    }

    @Override
    public void setValue(int ordinal, Object value) {
        throw new SofException("JoinRow do not support setValue");
    }
}
