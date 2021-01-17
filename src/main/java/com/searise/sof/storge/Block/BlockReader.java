package com.searise.sof.storge.Block;

import com.searise.sof.core.row.InternalRow;

public interface BlockReader extends AutoCloseable {
    InternalRow next() throws Exception;
    boolean hasNext() throws Exception;
    void close() throws Exception;
}
