package com.searise.sof.storge.disk;

import com.google.common.base.Preconditions;
import com.searise.sof.storge.Block;

import java.io.File;

public class DiskBlock implements Block {
    private final File file;

    DiskBlock(File file) {
        this.file = file;
    }

    @Override
    public int capacity() {
        return file.exists() ? Integer.MAX_VALUE : 0;
    }

    @Override
    public void free() {
        Preconditions.checkArgument(!file.exists() || file.delete());
    }
}
