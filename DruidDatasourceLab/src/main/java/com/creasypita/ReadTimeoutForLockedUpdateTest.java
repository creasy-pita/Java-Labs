package com.creasypita;

import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by lujq on 1/19/2024.
 * 模拟长时间运行的查询触发读取数据超时
 */
public class ReadTimeoutForLockedUpdateTest {

    private static final Logger logger = Logger.getLogger(ReadTimeoutForLockedUpdateTest.class);

    public static void main(String[] args) throws SQLException {
        logger.debug("开始---------------------------------------------------------");
        noMoreConnInThreadPool();
    }

    /**
     * 用户线程执行一个更新语句来更新记录，而记录已经被数据库的另一个用户会话锁住。因为锁超时的时间大于读取时间超时时间，所以会先触发读取时间超时
     */
    public static void noMoreConnInThreadPool() {
        new MyThread3().start();
    }
}
