# druid笔记

## 主要配置属性

|配置	|缺省值	|说明|
|-|-|-|
|name||配置这个属性的意义在于，如果存在多个数据源，监控的时候可以通过名字来区分开来。如果没有配置，将会生成一个名字，格式是："DataSource-" + System.identityHashCode(this). 另外配置此属性至少在1.0.5版本中是不起作用的，强行设置name会出错。详情-点此处。|
|initialSize|0|初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时|
|maxActive|8|最大连接池数量|
|maxIdle|8|已经不再使用，配置了也没效果|
|maxWait||获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。|
|validationQuery||		用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。|
|poolPreparedStatements|false|是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。|
|maxPoolPreparedStatementPerConnectionSize	|-1	|要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100|
|validationQueryTimeout|	|	单位：秒，检测连接是否有效的超时时间。底层调用jdbc Statement对象的void setQueryTimeout(int seconds)方法|
|testOnBorrow|	true	|申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。|
|testOnReturn	|false	|归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。|
|testWhileIdle	|false	|建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。|
|keepAlive|	false（1.0.28）|连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作。|
|timeBetweenEvictionRunsMillis	|1分钟（1.0.14）|	有两个含义：1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
|numTestsPerEvictionRun	|30分钟（1.0.14）|	不再使用，一个DruidDataSource只支持一个EvictionRun|
|minEvictableIdleTimeMillis	|	|连接保持空闲而不被驱逐的最小时间|
|connectionInitSqls	|	|物理连接初始化的时候执行的sql|
|exceptionSorter|	|根据dbType自动识别	当数据库抛出一些不可恢复的异常时，抛弃连接|
|removeAbandoned |false|开启强制回收使用中的连接的功能|
|removeAbandonedTimeout|| 连接使用时间（租期），超过这个没有关闭的就会强制回收|
|logAbandoned|false |执行强制关闭回收时，是否输出错误日志|

## 测试多线程获取连接时获取连接超时的情况

```properties{.line-numbers}
initialSize = 2
maxActive = 2
minIdle = 2
maxWait = 3000
removeAbandoned = false
```

- 第1，2行表示初始和最大活跃数2
- 第4表示等3秒

```log{.line-numbers}
May 20, 2023 5:41:44 PM com.alibaba.druid.pool.DruidDataSource info
INFO: {dataSource-1} inited
连接使用5秒
连接使用5秒
com.alibaba.druid.pool.GetConnectionTimeoutException: wait millis 3001, active 2, maxActive 2, creating 0
	at com.alibaba.druid.pool.DruidDataSource.getConnectionInternal(DruidDataSource.java:1734)
	at com.alibaba.druid.pool.DruidDataSource.getConnectionDirect(DruidDataSource.java:1404)
	at com.alibaba.druid.filter.FilterChainImpl.dataSource_connect(FilterChainImpl.java:5059)
	at com.alibaba.druid.filter.stat.StatFilter.dataSource_getConnection(StatFilter.java:680)
	at com.alibaba.druid.filter.FilterChainImpl.dataSource_connect(FilterChainImpl.java:5055)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:1382)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:1374)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:98)
	at com.creasypita.DruidUtil.getConnection(DruidUtil.java:36)
	at com.creasypita.MyThread.run(MyThread.java:15)
Exception in thread "Thread-0" java.lang.NullPointerException
	at com.creasypita.MyThread.run(MyThread.java:21)
com.alibaba.druid.pool.DruidPooledResultSet@6a6108b1
com.alibaba.druid.pool.DruidPooledResultSet@10f51557

Process finished with exit code 0

```

- 第5行表示thread "Thread-0"等待超过最大等待时间`maxWait = 3000`没有拿到连接，会抛出`GetConnectionTimeoutException`
- 第16行说明因为得到的是空连接，所有提示空指针

## RemoveAbandon测试

功能说明：使用的连接有租期，租期到了就回收。具体配置如下


```properties{.line-numbers}
maxActive = 2
minIdle = 2
maxWait = 30000
<!-- 开启强制回收使用中的连接的功能 -->
removeAbandoned = true
<!-- 连接使用时间（租期），超过这个没有关闭的就会强制回收 -->
removeAbandonedTimeout = 6
<!-- 执行强制回收时，是否打印信息日志 -->
logAbandoned = true
<!-- 这个也要设置，表示多久去执行一次回收检查 -->
timeBetweenEvictionRunsMillis = 1000
minEvictableIdleTimeMillis = 300000
```

- timeBetweenEvictionRunsMillis这个参数也要合理设置，这里设置为1000ms：表示多久去执行一次回收检查，检查了并检查到了才有后续强制回收的可能。所以必须合理设置

```log {.line-numbers}
May 20, 2023 5:33:31 PM com.alibaba.druid.pool.DruidDataSource info
INFO: {dataSource-1} inited
连接使用5秒
连接使用5秒
May 20, 2023 5:33:35 PM com.alibaba.druid.pool.DruidDataSource error
SEVERE: abandon connection, owner thread: Thread-2, connected at : 1684575211935, open stackTrace
	at java.lang.Thread.getStackTrace(Thread.java:1559)
	at com.alibaba.druid.pool.DruidDataSource.getConnectionDirect(DruidDataSource.java:1473)
	at com.alibaba.druid.filter.FilterChainImpl.dataSource_connect(FilterChainImpl.java:5059)
	at com.alibaba.druid.filter.stat.StatFilter.dataSource_getConnection(StatFilter.java:680)
	at com.alibaba.druid.filter.FilterChainImpl.dataSource_connect(FilterChainImpl.java:5055)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:1382)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:1374)
	at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:98)
	at com.creasypita.DruidUtil.getConnection(DruidUtil.java:36)
	at com.creasypita.MyThread.run(MyThread.java:15)
ownerThread current state is TIMED_WAITING, current stackTrace
	at java.lang.Thread.sleep(Native Method)
	at com.creasypita.MyThread.run(MyThread.java:33)
```

- 第5，6行表示租期到时，开始回收`Thread-2`，第17行展示了`Thread-2`的调用栈内容，和线程当前的状态是`TIMED_WAITING`
