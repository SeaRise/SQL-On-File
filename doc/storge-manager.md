- 存储管理
```
传统的火山模型理论上是不需要用到多少内存的,因为是一行数据通过多个算子处理,再处理下一行,内存可以说只是一行内存的占用量.
但是遇到join,sort,agg这些需要物化和shuffle的算子,就需要把数据存储起来做计算,需要用到大量内存.
原先在使用内存的时候,都是一股脑就分配了,但是这样子在数据量大的时候就容易oom.
所以需要做好存储管理:记录内存的使用量,将超量的内存数据溢出到磁盘使用,同时使用的各个数据结构也要改造能使用磁盘和内存数据做计算.
```
```
+--------+
| memory |
+--------+
    |
    | spill
    V
+--------+
|  disk  |
+--------+
```
- 组件:
	- MemoryManager
		- memoryPool: 内存使用计数
		- memoryAllocator: 分配内存块
	- diskManager: 分配磁盘空间,读写数据.
	- storageConsumer: 使用存储的消费者,各个计算/shuffle用的数据结构如:shuffleStore等都会继承它.
	- Block: 无论MemoryManager还是diskManager分配返回的都是Block, 仅存储模式不同, 用于读写.
```
+---------------------------------------------------------+
|                      storageConsumer                    |
|------------------+                     +----------------|
|   MemoryManager  | ----------------->  |   diskManager  |
|------------------+      spill          +----------------|
+---------------------------------------------------------+
```
- 实现
    - ShuffleStore
        - 用于写入shuffle data, 支持spill disk.
    - HashRelation
       - 用于hash join, 不支持spill disk.