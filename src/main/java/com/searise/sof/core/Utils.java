package com.searise.sof.core;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Utils {
    public static <T> List<T> toImmutableList(Stream<T> stream) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        stream.forEach(builder::add);
        return builder.build();
    }

    public static <T> List<T> combine(List<T> left, List<T> right) {
        if (left.isEmpty()) {
            return right;
        } else if (right.isEmpty()) {
            return left;
        } else {
            ImmutableList.Builder<T> builder = ImmutableList.builder();
            return builder.addAll(left).addAll(right).build();
        }
    }

    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new SofException(String.valueOf(errorMessage));
        }
    }

    public static List<String> split(String sqls) {
        if (StringUtils.isBlank(sqls)) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<String> sqlListBuilder = ImmutableList.builder();
        String[] sqlArr = StringUtils.split(sqls, ";");
        if (Objects.isNull(sqlArr) || 0 == sqlArr.length) {
            return ImmutableList.of();
        }

        StringBuilder sqlBuilder = new StringBuilder();
        for (String oneSql : sqlArr) {
            boolean continueFlag = false;
            if (StringUtils.endsWith(oneSql, "\\")) {
                sqlBuilder.append(StringUtils.chop(oneSql)).append(";");
                continueFlag = true;
            } else {
                sqlBuilder.append(oneSql);
            }
            String sql = sqlBuilder.toString();
            if (StringUtils.isBlank(sql)) {
                continueFlag = true;
            }
            if (!continueFlag) {
                sqlListBuilder.add(StringUtils.trim(sql));
                sqlBuilder.setLength(0);
            }
        }
        return sqlListBuilder.build();
    }

    public static String removeComments(String sql) throws IOException {
        if (StringUtils.isBlank(sql)) {
            return sql;
        }

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(sql.getBytes())));
        StringBuilder qsb = new StringBuilder();
        String line = bufferReader.readLine();
        while (line != null) {
            if (!line.startsWith("--")) {
                qsb.append(line).append("\n");
            }
            line = bufferReader.readLine();
        }
        return qsb.toString();
    }
}
