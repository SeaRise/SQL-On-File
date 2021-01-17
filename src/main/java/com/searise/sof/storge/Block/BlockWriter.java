package com.searise.sof.storge.Block;

import com.searise.sof.core.row.InternalRow;

public interface BlockWriter extends AutoCloseable {
    void write(InternalRow row) throws Exception;
    void close() throws Exception;
}
