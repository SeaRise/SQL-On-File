# SQL-On-Fil
用sql操作txt文件

```
           |------|                               |--------|                             
`sql` -->  |parser| --> `parsed logical plan` --> |analyzer| --> `resolved logical plan` ----+ 
           |------|                               |--------|                                 |
                                                                                             |
                                                                                             |
                                        |-------|                            |---------|     |
`get sql result` <---- `executor` <---- |builder| <---- `physical plan` <--- |optimizer| <---+
                                        |-------|                            |---------|
```

编译
```
mvn clean package
然后把生成的SQL-On-File-1.0-SNAPSHOT-jar-with-dependencies.jar放到bin目录下面,和sof.cmd同一目录.
ps: 已经预先生成好SQL-On-File-1.0-SNAPSHOT-jar-with-dependencies.jar.
```

快速使用:
在bin目录下面, 执行sof.cmd
- 首先执行ddl语句,创建实例表,表对应的txt文件在bin/example/example.txt
```
create table example (a int, b double, c bool, d string) path 'example/example.txt';
```
- 查看表是否建好
```
show tables;
```
- 然后可以执行sql查询
```
select a from example where a < 5;

select t1.a, t2.a, t1.b, t2.b, t1.c, t2.c, t1.d, t2.d from 
(select a, b, c, d from example where a > 90) t1 join 
(select a, b, c, d  from example where a < 10) t2 on (t1.a%10) = t2.a;
```
- 退出
```
exit;
```