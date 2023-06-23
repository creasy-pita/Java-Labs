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
|logAbandoned|false |执行强制回收时，是否打印信息日志|

## 测试初始连接数

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


## 获取连接时输出druid数据源的各种连接数（活跃连接，空闲连接）信息

通过DruidDataSource的以下方法获取各种连接数

        System.out.println("当前连接池活跃连接数为" +  ((DruidDataSource) dataSource).getActiveCount());
        System.out.println("当前连接池空闲连接数为" +   ((DruidDataSource) dataSource).getPoolingCount());


## 销毁线程的代码分析


还有非常重要的一点，这个存储连接的容器是有排序的，每次使用连接都从最后拿，这就导致容器尾部的连接是最活跃的，也就导致前面的连接闲置时间肯定是要高于后面的



maxActive 最大连接数
initialSize 初始化连接数
minIdle 最小空闲数
keepAlive 是否保持连接
asyncInit 是否异步初始化
timeBetweenEvictionRunsMillis 回收连接任务运行的频率
minEvictableIdleTimeMillis 最小闲置时间，连接闲置时间小于这个时间不会被回收，大于有可能被回收
maxEvictableIdleTimeMillis 最大闲置时间，连接闲置时间超过这个数是一定被回收的
validationQuery 测试是否有效的sql
phyTimeoutMillis 连接物理超时时间

connectTimeMillis 连接建立的时间
lastActiveTimeMillis 连接上一次被使用的时间


private int poolingCount = 0; // 可用连接数
private int activeCount = 0; // 正在使用连接数
private volatile long  discardCount = 0; // 丢弃连接数
private int notEmptyWaitThreadCount = 0; // 等待连接的线程数


```java
public void shrink(boolean checkTime, boolean keepAlive) {
    // 获取锁
    lock.lockInterruptibly();
    
    // 是否需要补充
    boolean needFill = false;
    // 驱逐的数量
    int evictCount = 0;
    // 需要保活的数量
    int keepAliveCount = 0;
    int fatalErrorIncrement = fatalErrorCount - fatalErrorCountLastShrink;
    fatalErrorCountLastShrink = fatalErrorCount;
    
    try {
        // 未初始化完成不执行
        if (!inited) {
            return;
        }

        // 池中可用连接数超出最小连接数的数量
        final int checkCount = poolingCount - minIdle;
        final long currentTimeMillis = System.currentTimeMillis();
        // 循环池中可用连接
        for (int i = 0; i < poolingCount; ++i) {
            DruidConnectionHolder connection = connections[i];

            // 异常的处理，暂不做考虑
            if ((onFatalError || fatalErrorIncrement > 0) && (lastFatalErrorTimeMillis > connection.connectTimeMillis))  {
                keepAliveConnections[keepAliveCount++] = connection;
                continue;
            }
            
            // 如果检查时间，销毁线程传入的是true
            if (checkTime) {
                // 如果设置了物联连接超时时间
                if (phyTimeoutMillis > 0) {
                    // 当前连接连接时间过过了超时时间，加入要待回收集合中
                    long phyConnectTimeMillis = currentTimeMillis - connection.connectTimeMillis;
                    if (phyConnectTimeMillis > phyTimeoutMillis) {
                        evictConnections[evictCount++] = connection;
                        continue;
                    }
                }

                // 计算当前连接已闲置的时间
                long idleMillis = currentTimeMillis - connection.lastActiveTimeMillis;

                // 如果连接闲置时间比较短，则可不被回收，可以直接跳出循环，因为连接池是尾部更活跃，后面的肯定更短不需要判断了
                if (idleMillis < minEvictableIdleTimeMillis
                        && idleMillis < keepAliveBetweenTimeMillis
                ) {
                    break;
                }

                // 如果连接闲置时间超出了设置的 最小闲置时间
                if (idleMillis >= minEvictableIdleTimeMillis) {
                    // 如果当前连接的位置在checkCount以内，则加入待回收集合
                    if (checkTime && i < checkCount) {
                        evictConnections[evictCount++] = connection;
                        continue;
                    // 否则如果已超出最大闲置时间，也要加入待回收集合  
                    } else if (idleMillis > maxEvictableIdleTimeMillis) {
                        evictConnections[evictCount++] = connection;
                        continue;
                    }
                }
				// <1> **如果连接闲置时间没有超出最小闲置时间 而且
                // 如果闲置时间超出保活检测时间，且设置了keepAlive，则加入待验证保活的集合中
                if (keepAlive && idleMillis >= keepAliveBetweenTimeMillis) {
                    keepAliveConnections[keepAliveCount++] = connection;
                }
            } else {
                //...
            }
        }

        // 要删除的连接总数，实际上keepAliveCount只是有可能被删除，还没有最终定论，这里做法是先删除掉，如果验证连接可用后续再加回来即可
        int removeCount = evictCount + keepAliveCount;
        if (removeCount > 0) {
            // 删除连接池中的废弃连接，由于废弃的连接一定是前removeCount个连接，所以直接使用复制即可删除
            System.arraycopy(connections, removeCount, connections, 0, poolingCount - removeCount);
            Arrays.fill(connections, poolingCount - removeCount, poolingCount, null);
            // 当前可用连接数变小
            poolingCount -= removeCount;
        }
        keepAliveCheckCount += keepAliveCount;

        // 如果设置了保活，且总连接数小于最小连接数，则需要补充
        if (keepAlive && poolingCount + activeCount < minIdle) {
            needFill = true;
        }
    } finally {
        lock.unlock();
    }

    // 如果有要回收的连接
    if (evictCount > 0) {
        // 循环
        for (int i = 0; i < evictCount; ++i) {
            DruidConnectionHolder item = evictConnections[i];
            Connection connection = item.getConnection();
            // 关闭连接
            JdbcUtils.close(connection);
            destroyCountUpdater.incrementAndGet(this);
        }
        // 清空需要回收的连接集合
        Arrays.fill(evictConnections, null);
    }

    // 如果有要进行保活的连接
    if (keepAliveCount > 0) {
        // 循环要保活的连接
        for (int i = keepAliveCount - 1; i >= 0; --i) {
            DruidConnectionHolder holer = keepAliveConnections[i];
            Connection connection = holer.getConnection();
            holer.incrementKeepAliveCheckCount();

            boolean validate = false;
            try {
                // 验证链接是否有效，此时要用到配置的validationQuery来验证连接的有效性，如果没设置，就默认有效
                this.validateConnection(connection);
                validate = true;
            } catch (Throwable error) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("keepAliveErr", error);
                }
            }

            boolean discard = !validate;
            // 如果连接有效
            if (validate) {
                holer.lastKeepTimeMillis = System.currentTimeMillis();
                // 重新加入连接池最左侧
                boolean putOk = put(holer, 0L, true);
                if (!putOk) {
                    discard = true;
                }
            }

            // 如果连接无效
            if (discard) {
                try {
                    // 关闭连接
                    connection.close();
                } catch (Exception e) {
                    // skip
                }

                lock.lock();
                try {
                    // 记录被丢弃的连接数+1
                    discardCount++;
                    // 如果且总连接数小于最小连接数，发出空信号
                    if (activeCount + poolingCount <= minIdle) {
                        emptySignal();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        this.getDataSourceStat().addKeepAliveCheckCount(keepAliveCount);
        // 清空需要保活的连接集合
        Arrays.fill(keepAliveConnections, null);
    }

    // 如果需要补充
    if (needFill) {
        lock.lock();
        try {
            // 计算需要补充的数量，createTaskCount是使用自定义调度时的逻辑，暂时忽略
            int fillCount = minIdle - (activeCount + poolingCount + createTaskCount);
            // 发出空信号
            for (int i = 0; i < fillCount; ++i) {
                emptySignal();
            }
        } finally {
            lock.unlock();
        }
    } else if (onFatalError || fatalErrorIncrement > 0) {
        // 异常处理 忽略..
    }
}
```


可以看出，有保活检测时间，最小闲置时间，最大闲置时间；保活检测时间一般需要比最小闲置时间才有意义，不然前者时间没到，后者时间到了，那么大概率这个连接就被驱逐销毁了，没有机会执行保活检查。

比如  保活检测时间=30s, 最小闲置时间=20s,最大闲置时间=300s;销毁线程执行时发现当前连接的闲置时间超出了最小闲置时间=20s，那此连接大概率会驱逐销毁，没有机会执行保活检查



## 复现数据库服务端主动掐掉连接，客户端这些连接与服务端通信发数据时报错的场景


### druid配置

```properties
driverClassName = com.mysql.jdbc.Driver
url = jdbc:mysql://192.168.100.66:3306/platform?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2b8&allowMultiQueries=true
username = root
password = 123456

initialSize=2
maxActive = 20
minActive = 2
minIdle = 5
maxWait = 30000

removeAbandoned = false
removeAbandonedTimeout = 6
validationQuery=select 1
# 2秒钟就去保活检查
keepAlive = true
keepAliveBetweenTimeMillis = 2000
logAbandoned = true

timeBetweenEvictionRunsMillis = 1000
minEvictableIdleTimeMillis = 300000
```

### mysql修改空闲超时`wait_timeout`

一、什么是8小时问题？
Mysql服务器默认的“ wait_timeout ”是8小时，也就是说一个connection空闲超过8个小时，Mysql将自动断开该 connection。这就是问题的所在，在Hibernate默认连接池中的connections如果空闲超过8小时，Mysql将其断开，而Hibernate默认连接池并不知道该connection已经失效，如果这时有 Client请求connection，Hibernate默认连接池将该失效的Connection提供给Client，将会造成上面的异常。

my.ini

```ini
# 最大空闲超时时间
wait_timeout=20
interactive_timeout = 20
```