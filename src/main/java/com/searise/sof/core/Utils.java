package com.searise.sof.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class Utils {
    public static List<File> listFiles(File dir) {
        Utils.checkArgument(!dir.exists() || dir.isDirectory(),
                String.format("path(%s) must be directory!", dir.getPath()));
        if (!dir.exists()) {
            return ImmutableList.of();
        }

        File[] files = dir.listFiles(Utils.fileFilter());
        if (Objects.isNull(files) || files.length <= 0) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(files);
    }

    public static FilenameFilter fileFilter() {
        return (dir, name) -> !StringUtils.startsWith(name, ".") &&
                !StringUtils.startsWith(name, "_");
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static <T> List<Pair<Integer, T>> zip(List<T> list) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<Pair<Integer, T>> builder = ImmutableList.builder();
        for (int i = 0; i < list.size(); i++) {
            builder.add(Pair.of(i, list.get(i)));
        }
        return builder.build();
    }

    public static Map<Long, Integer> zip(ExprIdGetter exprIdGetter, int size) {
        ImmutableMap.Builder<Long, Integer> build = ImmutableMap.builder();
        for (int index = 0; index < size; index++) {
            build.put(exprIdGetter.apply(index), index);
        }
        return build.build();
    }

    public static <T> List<T> toImmutableList(Stream<T> stream) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        stream.forEach(builder::add);
        return builder.build();
    }

    @SafeVarargs
    public static <T> List<T> combine(List<T>... lists) {
        List<T> builder = new ArrayList<>();
        for (List<T> list : lists) {
            builder.addAll(list);
        }
        return toImmutableList(builder.stream());
    }

    @SafeVarargs
    public static <T> List<T> combineDistinct(List<T>... lists) {
        List<T> builder = new ArrayList<>();
        for (List<T> list : lists) {
            builder.addAll(list);
        }
        return toImmutableList(builder.stream().distinct());
    }

    public static <T> Set<T> combineDistinct(Set<T> left, Set<T> right) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        builder.addAll(left);
        builder.addAll(right);
        return builder.build();
    }

    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new SofException(String.valueOf(errorMessage));
        }
    }

    public static <T> T checkNotNull(T obj, @Nullable Object errorMessage) {
        if (Objects.isNull(obj)) {
            throw new SofException(String.valueOf(errorMessage));
        }
        return obj;
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

    public static int nonNegativeHash(Object obj) {
        if (Objects.isNull(obj)) {
            return 0;
        }

        int hash = obj.hashCode();
        // math.abs fails for Int.MinValue
        return Integer.MIN_VALUE != hash? Math.abs(hash) : 0;
    }
}
