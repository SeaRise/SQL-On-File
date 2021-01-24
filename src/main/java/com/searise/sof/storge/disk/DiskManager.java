package com.searise.sof.storge.disk;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class DiskManager implements AutoCloseable {
    private final DiskOption option = new DiskOption();

    public DiskBlock allocate() throws IOException {
        File blockFile = genDiskBlockFile();
        return new DiskBlock(blockFile);
    }

    public void free(DiskBlock block) {
        block.free();
    }

    private File genDiskBlockFile() throws IOException {
        String blockId = option.unitFilePrefix + UUID.randomUUID().toString();
        int subDirIndex = Utils.nonNegativeHash(blockId) % option.subDirs;
        File subDir = new File(option.baseDir + subDirIndex);
        if (!subDir.exists()) {
            Files.createDirectory(subDir.toPath());
        }
        File blockFile = new File(subDir, UUID.randomUUID().toString());
        Preconditions.checkArgument(!blockFile.exists());
        return blockFile;
    }

    @Override
    public void close() throws Exception {
        Files.deleteIfExists(new File(option.baseDir).toPath());
    }

    private class DiskOption {
        private final String unitFilePrefix = "sof_";
        private final String baseDir = System.getProperty("java.io.tmpdir") +
                unitFilePrefix + UUID.randomUUID().toString() + File.pathSeparatorChar;
        private final int subDirs = 16;
    }
}
