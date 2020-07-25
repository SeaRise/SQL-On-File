## parser

#### 语句:
- create table / create table as select sql
- insert overwrite table select sql
- select sql

#### create
```
- grammer
1/create table table_name (field1 dataType1, field2 dataType2, ...) path 'path' [separatot 'separator'];
2/create table table_name as select sql

- example
1/create table example (a int, b double, c bool, d string) path 'example\example.txt';
2/create table output path 'example\output.txt' as select a, b, c, d from example;
```

#### insert
```
- grammer
insert overwrite table table_name select sql
- example
insert overwrite table output select a, b, c, d from example;
```

#### select
- 支持join, select, where, from table4种算子
```
- grammer
1/select field1,field2, field3, ... from table
2/select fields from table where cond1 and cond2 or cond3 and not (cond4)
3/select fields from table1 join table2 on condtitions
4/select fields from table1, table2

- example
select t1.a, t1.b, t1.c, t1.d from 
(select a, b, c, d from example where a > 90) t1 join 
(select a, b, c, d  from example where a < 10) t2 on (t1.a%10) = t2.a;
```

#### 运算符号
```
1/ +, -, *, %, 负号-
2/ >, >=, <, <=, =
3/ and, or, not
```

#### 设计
```
用antlr4解析语法.
参考了`SQL-Engine-On-LSM`和`spark-sql`.
.g4是在`SQL-Engine-On-LSM`的.g4基础上改出来的.

parser把ast转成logical plan.
在设计logical plan的时候,我是确定了一个原则: 
logical plan是immutable的,如果要修改logical plan, 必须new一个.
包括expression也是如此.
```