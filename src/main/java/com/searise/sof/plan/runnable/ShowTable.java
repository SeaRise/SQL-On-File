package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ShowTable implements LogicalPlan, RunnableCommand {
    public final SofContext context;

    public ShowTable(SofContext context) {
        this.context = context;
    }

    @Override
    public void run(Catalog catalog) {
        List<String> allTable = catalog.getAllTable();
        if (allTable.isEmpty()) {
            Utils.println("--\n||\n--");
            return;
        }

        int maxLen = allTable.stream().map(String::length).max(Integer::compareTo).orElse(0);
        if (maxLen <= 0) {
            Utils.println("----\n|  |\n----");
            return;
        }

        String topOrBottom = StringUtils.repeat('-', maxLen + 2);
        Utils.println(topOrBottom);
        for (String table : allTable) {
            Utils.println("|" + centerToString(table, maxLen) + "|");
        }
        Utils.println(topOrBottom);
    }

    private String centerToString(String str, int maxLen) {
        int len = str.length();
        int left = (maxLen - len) / 2;
        return StringUtils.repeat(' ', left) + str + StringUtils.repeat(' ', maxLen - left - len);
    }

    @Override
    public SofContext context() {
        return context;
    }
}
