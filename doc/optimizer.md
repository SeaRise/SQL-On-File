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
					   |------------|		                  |---------|
```

#### preprocess
```

```

#### explore
```

```

#### implement
```

```

#### afterprocess
```

```