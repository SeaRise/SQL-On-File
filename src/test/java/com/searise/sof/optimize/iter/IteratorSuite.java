package com.searise.sof.optimize.iter;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IteratorSuite {
    @Test
    public void test() {
        //foreach
        List<Integer> tests = Arrays.asList(0, 1, 2, 3, 4);
        Iterator<Integer> iterator = new Iterator<>();
        for (Integer test : tests) {
            iterator.add(test);
        }
        int index = 0;
        while (iterator.hasNext()) {
            int res = iterator.next();
            Preconditions.checkArgument(tests.get(index++).equals(res));
        }

        tests = Arrays.asList(0, 1, 2, 3, 4, 5);
        iterator.reset();
        index = 0;
        while (iterator.hasNext()) {
            int res = iterator.next();
            Preconditions.checkArgument(tests.get(index++).equals(res));
            if (index == 5) {
                iterator.add(5);
            }
        }

        iterator.reset();
        while (iterator.hasNext()) {
            if (iterator.next() == 3) {
                iterator.remove();
            }
        }
        tests = Arrays.asList(0, 1, 2, 4, 5);
        iterator.reset();
        index = 0;
        while (iterator.hasNext()) {
            Preconditions.checkArgument(tests.get(index++).equals(iterator.next()));
        }

        iterator.reset();
        while (iterator.hasNext()) {
            if (iterator.next() == 3) {
                iterator.remove();
            }
        }
        tests = Arrays.asList(0, 1, 2, 4, 5, 3);
        iterator.reset();
        index = 0;
        while (iterator.hasNext()) {
            Preconditions.checkArgument(tests.get(index++).equals(iterator.next()));
        }

        Iterator<Integer> readIter = iterator.newReadOnlyIter();
        index = 0;
        while (readIter.hasNext()) {
            Preconditions.checkArgument(tests.get(index++).equals(readIter.next()));
        }

        iterator.reset();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Preconditions.checkArgument(!iterator.reset().hasNext());
    }
}
