package com.searise.sof.core;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UtilsSuite {
    @Test
    public void testImmutableCollector() {
        List<Integer> origin = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> res = Utils.toImmutableList(origin.stream());
        Preconditions.checkArgument(origin.size() == res.size());
        for (int i = 0; i < origin.size(); i++) {
            Preconditions.checkArgument(origin.get(i) == res.get(i));
        }
    }
}
