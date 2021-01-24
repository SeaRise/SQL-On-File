package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.TestSession;
import com.searise.sof.core.SofSession;
import org.junit.Test;

public class InsertSuite {

    @Test
    public void testInsertOverwrite() throws Exception {
        try (SofSession session = TestSession.newTestSession()) {
            String createSql1 = "create table input_table (a double, b double, c double, d double) path 'src\\test\\resources\\in'";
            String createSql2 = "create table output_table (a double, b double, c double, d double) path 'src\\test\\resources\\out'";
            session.compile(createSql1);
            session.compile(createSql2);

            String sql = "insert overwrite table output_table select a, b, c, d from input_table";
            session.compile(sql);
        }
    }

    @Test
    public void testCreateTableAsSelect() throws Exception {
        try (SofSession session = TestSession.newTestSession()) {
            String createSql1 = "create table input_table (a double, b double, c double, d double) path 'src\\test\\resources\\in'";
            session.compile(createSql1);

            String sql = "create table output_table path 'src\\test\\resources\\out' as select a, b, c, d from input_table";
            session.compile(sql);
        }
    }
}
