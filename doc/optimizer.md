## optimizer
基本是对TiDB cascade planner的模仿...
基本上是复现了整个cascade optimize的流程,但是忽略了一些细节:
- property.PhysicalProperty, 因为没有实现sort算子,所以也就不需要这个.
- cost limit, cost limit这个本质上是dfs搜索有向无环图.前提是预先定义好算子的cost,在TiDB中考虑到了cpu和内存等等.但是在这里cost怎么定义我还没想好...所以就简单的选最短的路径作为最优路径.

cascade planner的介绍可以看:https://pingcap.com/blog-cn/tidb-cascades-planner/

#### 流程
```
                   |----------|                       |-------|    
`logical plan` --> |preprocess|--> `logical plan` --> |explore| --> `group` -+
                   |----------|                       |-------|              |
									     |
                       |------------|                         |---------|    |
   `physical plan` <-- |afterprocess| <-- `physical plan` <-- |implement| <--+
                       |------------|                         |---------|
```

#### preprocess
```
这里面放了一些我感觉没必要在explore的rule.
TiDB放的是prune columns,但是我把prune columns放到了afterprocess.
TiDB在preprocess放prune columns,大概是为了对dataSource用到的列做列裁剪,这样在选择IndexScan的时候,就可以知道要不要回表.
但是我这里没有索引的实现,所以只需要在最后做一次prune columns.这样就不用列裁剪两次.

RemoveSubqueryAlias
RemoveUselessAlias
这两个PreprocessRule的作用如字面意思.
```

#### explore
```
两个rule-batch:
1/ 谓词下推,project消除.
2/ join reorder.
```

##### join reorder
```
join reorder由两个rule-batch组成
1/ 把join-tree压成multi-join.
2/ 把multi-join展开成ordered join.

目前只实现了贪心的join reorder,实现类是GreedyJoinReorder.做法和赫夫曼树差不多,每次循环选择cost最小的join plan.
cost的算法是参照spark-sql的算法;
cost = self-cost + left-cost + right-cost = (left-sizeInBytes + right-sizeInBytes) + left-cost + right-cost

sizeInBytes的计算也是参照spark-sql的sizeInByteVisitor,不过没有直方图的实现..因为这个要做数据源采样....
```

##### sizeInBytes
```
实现类是SizeInBytesStatsVisitor.
因为没有实现直方图,所以做了一个粗暴的假设: 单个条件(不包含not,and,or)的选择率为0.8.
p(and) = p(left) * p(right)
p(or) = p(left) + p(right) - (p(left) * p(right))
p(not) = 1 - p(child)

当初为啥这么定还有一个考虑.就是在cascade planner中.group中的每一个group-expr,计算出来的行数,都应该是一致的
比如filter-projec和filter1-project-filter2,计算出来的行数都应该一致.
只要filter.condition.size = filter1.condition.size + filter2.condition.size, 那按上面的算法,行数就一致.
所以做谓词下推的时候,要注意,条件必须完全拆分,不能重复.
所以会改变expression的rule我都不会放在这里,比如FoldExpression.
因为改变了expression,可能就会改变同一个group下面不同group-expr计算出来的行数.
```

#### implement
```
在得到explore后的group后,做dfs,不过是选的最短的路径作为最优路径.
目前join有两种实现: NestedLoopJoin和HashJoin.
ImplJoin会根据left-sizeInBytes和right-sizeInBytes选择join实现.
```

#### afterprocess
```
afterprocess做了三件事:
- prune columns
- resolve index
- remove alias
这三件事的顺序不能颠倒.

prune columns就是做列裁剪.
resolve index是要把Attribute转为BoundReference.在执行的时候需要知道Attribute读取下层算子传来的row的哪个下标的值.
remove alias把无用的Alias都消除掉,因为前面做resolve index需要用到alias,所以必须在resolve index之后做.

afterprocess和preprocess都做了remove alias,但是preprocess只是remove Alias(Attribute).afterprocess remove所有Alias, 包括Alias(function).
```
