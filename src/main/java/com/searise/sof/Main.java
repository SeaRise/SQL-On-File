package com.searise.sof;

import com.searise.sof.parser.SqlParser;

public class Main {
    public static void main(String[] args) {
        String sql = "select a from b where 'a' = 'v'";
        SqlParser sqlParser = new SqlParser();
        System.out.println(sqlParser.parsePlan(sql).visitToString());
    }
}
