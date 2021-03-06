## executor
其实就是经典的volcano模型的实现.
```
while (child.hasNext()) {
    val row = child.next()
    return handle(row);
}
```

#### executor
```
大多数的exector的实现都很简单,没啥好说的.
joinExecutor有NestedLoopJoinExec和HashJoinExec两种实现.
```

##### NestedLoopJoinExec
```
for outer-row : outer-table {
    for inner-row : inner-table {
        join(outer-row, inner-row)
    }
}
```

##### HashJoinExec
```
val hashTable = buildHashTable(inner-table)
for outer-row : outer-table {
    val joinKey = getJoinKey(outer-row)
    val inner-row = hashTable.get(joinKey)
    join(outer-row, inner-row)
}
```

#### schema
```
类似tidb inline-project的做法.
对于join/filter,conditions用到的attribute不一定要导出.
所以executor有schemaProjection,在join/filter执行完后,做一次筛选.
```

#### expression
```
expression和常规的实现一样,基于expression-tree去推动执行.
expression有两个接口Object eval(InternalRow row)和List<Expression> children();
比如function.eval = doFunc(child1.eval(row), child2.eval(row), ...);

expression-tree的最末端就是literal和BoundReference.
literal.eval就是返回自己的value.
BoundReference根据前面resolveIndex得到的下标去row取对应的值.
```

##### codegen
```
expression-tree的执行方式, 会带来大量递归调用,虚函数调用等等..
所以这里用codegen生成expression,消除children.eval调用.

参考spark-sql expression codegen的实现,用字符串拼接出代码,然后用janino生成CodegenExpression.
每个expression会实现codegen接口,最终在CodeGenerator生成CodegenExpression.

executor的codegen基本也差不多, 目前只有Filter和Projection实现了Codegen接口.
```