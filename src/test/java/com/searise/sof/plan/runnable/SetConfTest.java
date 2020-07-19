package com.searise.sof.plan.runnable;

import com.google.common.base.Preconditions;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class SetConfTest {
    @Test
    public void test() {
        String sql = "set key=value";
        Catalog catalog = new BuiltInCatalog();
        Context context = new Context();
        LogicalPlan parsePlan = new SqlParser(context).parsePlan(sql);
        SetCommand setCommand = (SetCommand) parsePlan;
        setCommand.run(catalog);
        Preconditions.checkArgument(StringUtils.equals(context.conf.getConf("key"), "value"));
    }
}
