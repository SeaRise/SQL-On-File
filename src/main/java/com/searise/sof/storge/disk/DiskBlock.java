package com.searise.sof.storge.disk;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.EmptyRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.storge.Block.Block;
import com.searise.sof.storge.Block.BlockReader;
import com.searise.sof.storge.Block.BlockWriter;
import com.searise.sof.type.DataType;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class DiskBlock implements Block {
    private final File file;

    DiskBlock(File file) {
        this.file = file;
    }

    public boolean hasUsed() {
        return file.exists() && file.length() > 0;
    }

    @Override
    public int capacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void free() {
        Preconditions.checkArgument(!file.exists() || file.delete());
    }

    @Override
    public BlockReader getReader() throws Exception {
        Preconditions.checkArgument(file.exists());
        return new BlockReader() {
            private FileInputStream fileIS = new FileInputStream(file);
            private DataInputStream dataIS = new DataInputStream(new BufferedInputStream(fileIS));
            private final long length = file.length();
            private long position = 0L;
            private InternalRow row = EmptyRow.EMPTY_ROW;

            @Override
            public InternalRow next() throws Exception {
                int fieldNum = dataIS.readInt();
                position += 4;
                if (fieldNum == 0) {
                    return EmptyRow.EMPTY_ROW;
                }
                if (row.numFields() != fieldNum) {
                    row = new ArrayRow(fieldNum);
                }
                for (int i = 0; i < fieldNum; i++) {
                    position += 1;
                    switch (DataType.getType(dataIS.readByte())) {
                        case DoubleType:
                            position += 8;
                            row.setDouble(i, dataIS.readDouble());
                            break;
                        case IntegerType:
                            position += 4;
                            row.setInt(i, dataIS.readInt());
                            break;
                        case StringType:
                            int byteSize = dataIS.readInt();
                            position += (4 + byteSize);
                            byte[] bytes = new byte[byteSize];
                            dataIS.readFully(bytes);
                            row.setString(i, new String(bytes));
                            break;
                        case BooleanType:
                            position += 1;
                            row.setBoolean(i, dataIS.readBoolean());
                            break;
                    }
                }
                return row;
            }

            @Override
            public boolean hasNext() throws Exception {
                return position < length;
            }

            @Override
            public void close() throws Exception {
                dataIS.close();
                fileIS.close();
            }
        };
    }

    @Override
    public BlockWriter getWriter() throws Exception {
        if (hasUsed()) {
            Preconditions.checkArgument(file.delete());
        }
        return new BlockWriter() {
            private FileOutputStream fileOS = new FileOutputStream(file);
            private final FileChannel fileChannel = fileOS.getChannel();
            private DataOutputStream dataOS = new DataOutputStream(fileOS);

            @Override
            public void write(InternalRow row) throws Exception {
                dataOS.writeInt(row.numFields());
                for (int i = 0; i < row.numFields(); i++) {
                    DataType dataType = row.getType(i);
                    dataOS.writeByte(dataType.getFlag());
                    switch (dataType) {
                        case DoubleType:
                            dataOS.writeDouble(row.getDouble(i));
                            break;
                        case IntegerType:
                            dataOS.writeInt(row.getInt(i));
                            break;
                        case StringType:
                            String str = row.getString(i);
                            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                            Preconditions.checkArgument(bytes.length == str.length());
                            dataOS.writeInt(bytes.length);
                            dataOS.write(bytes);
                            break;
                        case BooleanType:
                            dataOS.writeBoolean(row.getBoolean(i));
                            break;
                    }
                }
            }

            @Override
            public void close() throws Exception {
                dataOS.close();
                fileChannel.close();
                fileOS.close();
            }
        };
    }
}
