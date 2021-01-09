package com.searise.sof.shuffle.io;

import com.google.common.base.Joiner;
import com.searise.sof.core.row.InternalRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NewShuffleStore {
    private final List<InternalRow>[] shuffleBlocks;
    @SuppressWarnings("unchecked")
    public NewShuffleStore(int partitions) {
        this.shuffleBlocks = new List[partitions];
        for (int i = 0; i < shuffleBlocks.length; i++) {
            shuffleBlocks[i] = new ArrayList<>();
        }
    }

    public void write(int partition, InternalRow internalRow) {
        List<InternalRow> shuffleBlock = shuffleBlocks[partition];
        synchronized (shuffleBlock) {
            shuffleBlock.add(internalRow);
        }
    }

    public Iterator<InternalRow> read(int partition) {
        List<InternalRow> shuffleBlock = shuffleBlocks[partition];
        synchronized (shuffleBlock) {
            return shuffleBlock.iterator();
        }
    }

    public boolean isEmpty(int partition) {
        List<InternalRow> shuffleBlock = shuffleBlocks[partition];
        synchronized (shuffleBlock) {
            return shuffleBlock.isEmpty();
        }
    }

    public void cleanUp() {
        for (List<InternalRow> shuffleBlock : shuffleBlocks) {
            synchronized (shuffleBlock) {
                shuffleBlock.clear();
            }
        }
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(shuffleBlocks);
    }
}
