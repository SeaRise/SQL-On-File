package com.searise.sof.execution;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofException;
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
    private FileLineReader fileLineReader;
    private String curLine;
    private final InternalRow output;
    private final List<InternalRowWriter> writers;

    public ScanExec(List<BoundReference> schema, String filePath, String separator) {
        this.schema = schema;
        this.filePath = filePath;
        this.separator = separator;
        this.output = new ArrayRow(schema.size());
        ImmutableList.Builder<InternalRowWriter> writerBuilder = ImmutableList.builder();
        for (int index = 0; index < schema.size(); index++) {
            writerBuilder.add(InternalRow.getWriter(index, schema.get(index).dataType));
        }
        this.writers = writerBuilder.build();
    }

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

    private void throwFileFormatException() {
        throw new SofException(String.format("file line format is illegal: %s", curLine));
    }

    @Override
    public InternalRow next() {
        String[] splits = StringUtils.split(curLine, separator);
        if (Objects.isNull(splits) || splits.length < schema.size()) {
            throwFileFormatException();
        }


        for (int index = 0; index < schema.size(); index++) {
            BoundReference reference = schema.get(index);
            String str = getString(splits,  reference.index());
            writers.get(index).apply(output, convertDataType(str, reference.dataType));
        }

        curLine = fileLineReader.readLine();
        return output;
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

    private String getString(String[] splits, int index) {
        if (splits.length <= index) {
            throwFileFormatException();
        }
        return splits[index];
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
