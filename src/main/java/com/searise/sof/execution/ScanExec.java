package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.type.DataType;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ScanExec implements Executor {
    private final List<BoundReference> schema;
    private final String filePath;
    private final String separator;
    public final Context context;

    public ScanExec(List<BoundReference> schema, String filePath, String separator, Context context) {
        this.schema = schema;
        this.filePath = filePath;
        this.separator = separator;
        this.context = context;
    }

    @Override
    public RowIterator compute(int partition) {
        return new RowIterator() {
            private FileLineReader fileLineReader;
            private String curLine;
            private final InternalRow output = new ArrayRow(schema.size());
            private final List<InternalRowWriter> writers = Utils.toImmutableList(
                    Utils.zip(schema).stream().map(pair -> InternalRow.getWriter(pair.getLeft(), pair.getRight().dataType)));

            @Override
            public void open() {
                try {
                    this.fileLineReader = new FileLineReader(filePath);
                    curLine = fileLineReader.readLine();
                } catch (FileNotFoundException e) {
                    throw new SofException(String.format("ScanExec can not open file: %s", filePath));
                }
            }

            @Override
            public boolean hasNext() {
                return StringUtils.isNotBlank(curLine);
            }

            @Override
            public InternalRow next() {
                String[] splits = StringUtils.split(curLine, separator);
                if (Objects.isNull(splits) || splits.length < schema.size()) {
                    throwFileFormatException(curLine);
                }

                for (int index = 0; index < schema.size(); index++) {
                    BoundReference reference = schema.get(index);
                    String str = getString(splits, reference.index(), curLine);
                    writers.get(index).apply(output, convertDataType(str, reference.dataType));
                }

                curLine = fileLineReader.readLine();
                return output;
            }

            @Override
            public void close() {
                if (Objects.nonNull(fileLineReader)) {
                    try {
                        fileLineReader.close();
                    } catch (IOException e) {
                        throw new SofException(e.getMessage());
                    }
                }
            }
        };
    }

    private void throwFileFormatException(String line) {
        throw new SofException(String.format("file line format is illegal: %s", line));
    }

    private Object convertDataType(String str, DataType dataType) {
        switch (dataType) {
            case BooleanType:
                return Boolean.valueOf(str);
            case StringType:
                return str;
            case DoubleType:
                return Double.valueOf(str);
            case IntegerType:
                return Integer.valueOf(str);
            default:
                throw new SofException(String.format("unsupported dataType[%s] in convertDataType", dataType));
        }
    }

    private String getString(String[] splits, int index, String line) {
        if (splits.length <= index) {
            throwFileFormatException(line);
        }
        return splits[index];
    }

    @Override
    public List<Executor> children() {
        return ImmutableList.of();
    }

    @Override
    public Executor copyWithNewChildren(List<Executor> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.isEmpty());
        return this;
    }

    private class FileLineReader {
        private final FileReader fileReader;
        private final BufferedReader bufferedReader;

        private FileLineReader(String filePath) throws FileNotFoundException {
            this.fileReader = new FileReader(filePath);
            this.bufferedReader = new BufferedReader(fileReader);
        }

        public String readLine() {
            try {
                return bufferedReader.readLine();
            } catch (Exception e) {
                return "";
            }
        }

        public void close() throws IOException {
            bufferedReader.close();
            fileReader.close();
        }
    }
}
