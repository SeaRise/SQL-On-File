package com.searise.sof.plan.runnable;

import com.searise.sof.Driver;
import org.junit.Test;

public class InsertSuite {

    @Test
    public void testInsertOverwrite() throws Exception {
        Driver driver = new Driver();
        String createSql1 = "create table input_table (a double, b double, c double, d double) path 'src\\test\\resources\\in'";
        String createSql2 = "create table output_table (a double, b double, c double, d double) path 'src\\test\\resources\\out'";
        driver.compile(createSql1);
        driver.compile(createSql2);

        String sql = "insert overwrite table output_table select a, b, c, d from input_table";
        driver.compile(sql);
    }

    @Test
    public void testCreateTableAsSelect() throws Exception {
        Driver driver = new Driver();
        String createSql1 = "create table input_table (a double, b double, c double, d double) path 'src\\test\\resources\\in'";
        driver.compile(createSql1);

        String sql = "create table output_table path 'src\\test\\resources\\out' as select a, b, c, d from input_table";
        driver.compile(sql);
    }
}
