package com.searise.sof.storge.Block;

public interface Block {
    int capacity();

    void free();

    default BlockReader getReader() throws Exception {
        throw new UnsupportedOperationException();
    }

    default BlockWriter getWriter() throws Exception {
        throw new UnsupportedOperationException();
    }
}
