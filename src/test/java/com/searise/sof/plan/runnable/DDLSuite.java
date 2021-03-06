package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.TestContext;
import com.searise.sof.core.SofContext;
import com.searise.sof.parser.SqlParser;
import org.junit.Test;

public class DDLSuite {
    @Test
    public void test() {
        try (SofContext context = TestContext.newTestContext()) {
            Catalog catalog = new BuiltInCatalog();
            showTable(catalog);
            addTable("create table a (a int) path '/'", catalog);
            showTable(catalog);
            addTable("create table test_long_table_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa  (a int) path '/' separator ','", catalog);
            showTable(catalog);
        }
    }

    private void addTable(String sql, Catalog catalog) {
        SqlParser sqlParser = new SqlParser(SofContext.getOrCreate());
        CreateTable createTable = (CreateTable) sqlParser.parsePlan(sql);
        createTable.run(catalog);
    }

    public void showTable(Catalog catalog) {
        SqlParser sqlParser = new SqlParser(SofContext.getOrCreate());
        ShowTable showTable = (ShowTable) sqlParser.parsePlan("show tables");
        showTable.run(catalog);
    }
}
