package com.searise.sof.storge.memory;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.EmptyRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.Block.BlockReader;
import com.searise.sof.storge.Block.BlockWriter;
import com.searise.sof.type.Bytes;
import com.searise.sof.type.DataType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MemoryBlock implements Block {
    ByteBuffer byteBuffer;
    final int allocatedSize;
    // 是否有实际分配内存.
    private final boolean isAllocated;

    MemoryBlock(ByteBuffer byteBuffer, int allocatedSize) {
        this(byteBuffer, allocatedSize, true);
    }

    private MemoryBlock(ByteBuffer byteBuffer, int allocatedSize, boolean isAllocated) {
        this.byteBuffer = byteBuffer;
        this.allocatedSize = allocatedSize;
        this.isAllocated =  isAllocated;
    }

    @Override
    public int capacity() {
        return isAllocated ?
                (Objects.isNull(byteBuffer) ? 0 : byteBuffer.capacity()) :
                allocatedSize;
    }

    @Override
    public void free() {
        // just gc
        this.byteBuffer = null;
    }

    public boolean isAllocated() {
        return isAllocated;
    }

    // 用于创建没有实际分配内存的MemoryBlock.
    // 用于释放分配时没有走allocator的内存.
    public static MemoryBlock createNoAllocated(int require) {
        return new MemoryBlock(null, require, false);
    }

    public BlockReader getReader() throws Exception {
        Preconditions.checkArgument(isAllocated && Objects.nonNull(byteBuffer));
        ByteBuffer readBuffer = byteBuffer.asReadOnlyBuffer();
        readBuffer.flip();
        return new BlockReader() {
            private InternalRow row = EmptyRow.EMPTY_ROW;
            @Override
            public InternalRow next() throws Exception {
                int fieldNum = readBuffer.getInt();
                if (fieldNum == 0) {
                    return EmptyRow.EMPTY_ROW;
                }
                if (row.numFields() != fieldNum) {
                    row = new ArrayRow(fieldNum);
                }
                for (int i = 0; i < fieldNum; i++) {
                    switch (DataType.getType(readBuffer.get())) {
                        case DoubleType:
                            row.setDouble(i, readBuffer.getDouble());
                            break;
                        case IntegerType:
                            row.setInt(i, readBuffer.getInt());
                            break;
                        case StringType:
                            int byteSize = readBuffer.getInt();
                            row.setString(i, new String(readBuffer.array(), readBuffer.position(), byteSize));
                            readBuffer.position(readBuffer.position() + byteSize);
                            break;
                        case BooleanType:
                            row.setBoolean(i, Bytes.byteToBoolean(readBuffer.get()));
                            break;
                    }
                }
                return row;
            }

            @Override
            public boolean hasNext() throws Exception {
                return readBuffer.hasRemaining();
            }

            @Override
            public void close() throws Exception {
            }
        };
    }

    public BlockWriter getWriter() throws Exception {
        Preconditions.checkArgument(isAllocated && Objects.nonNull(byteBuffer));
        byteBuffer.reset();
        return new BlockWriter() {
            @Override
            public void write(InternalRow row) throws Exception {
                byteBuffer.putInt(row.numFields());
                for (int i = 0; i < row.numFields(); i++) {
                    DataType dataType = row.getType(i);
                    byteBuffer.put(dataType.getFlag());
                    switch (dataType) {
                        case DoubleType:
                            byteBuffer.putDouble(row.getDouble(i));
                            break;
                        case IntegerType:
                            byteBuffer.putInt(row.getInt(i));
                            break;
                        case StringType:
                            String str = row.getString(i);
                            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                            Preconditions.checkArgument(bytes.length == str.length());
                            byteBuffer.putInt(bytes.length);
                            byteBuffer.put(bytes);
                            break;
                        case BooleanType:
                            byteBuffer.put(Bytes.booleanToByte(row.getBoolean(i)));
                            break;
                    }
                }
            }

            @Override
            public void close() throws Exception {
            }
        };
    }
}
