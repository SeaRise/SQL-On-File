package com.searise.sof.codegen;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.operator.Add;
import com.searise.sof.type.DataType;
import org.junit.Test;

public class CodegenSuite {

    @Test
    public void test() {
        BoundReference left = new BoundReference(DataType.IntegerType, 0);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.IntegerType, 1);
        right.resolveIndex(1);
        Add add = new Add(left, right);
        System.out.println(add.genCode());
    }
}
