## analyzer
###### 和其他sql引擎的设计差不多.在parser解析出logical plan, 要对logical plan对元数据做绑定.
- resolve relation, 获取字段等等信息.
- resolve function, 把function转为实际的class.
- resolve attribute, 获取每一个attribute的dataType, 给每一个attribute分配exprId, 不再用名字来指代attribute.

###### 除此以外,我还把某些别的rule放在了这里.
- FoldExpression, children都是literal的function可以折叠成literal.
- pushDownNot, 把not算子尽量推到expression-tree的最底层.主要是为SplitCNF服务,把not(cond1 or cond2)分解为not(cond1) and not(cond2)
- SplitCNF, 把cond1 and cond2分解为list{cond1, cond2}.为join,filter算子服务,后面做谓词下推时,就只考虑list里的每一个item.

###### analyzer
```
analyzer参考spark-sql的设计, 分多个batch, 每一个batch循环apply rule,直到plan没有改变,才结束这个batch.
前面说的logical plan immutable的好处也在这里体现了.因为修改logical plan就会new一个,
所以我可以简单的用==判断指针地址来知道logical plan apply前后有无改变.
```
