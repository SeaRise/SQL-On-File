package com.searise.sof.plan.runnable;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofSession;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.SofContext;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Optional;

public class SetConfSuite {
    @Test
    public void test() {
        String sql = "set key=value";
        Catalog catalog = new BuiltInCatalog();
        SofContext context = SofContext.getOrCreate();
        LogicalPlan parsePlan = new SqlParser(context).parsePlan(sql);
        SetCommand setCommand = (SetCommand) parsePlan;
        setCommand.run(catalog);
        Optional<String> confValue = context.conf.getConf("key");
        Preconditions.checkArgument(confValue.isPresent());
        Preconditions.checkArgument(StringUtils.equals(confValue.get(), "value"));
    }
}
