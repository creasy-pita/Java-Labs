package com.creasypita;

import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by lujq on 1/19/2024.
 * 模拟数据库更新是排他锁等待超时的场景
 */
public class LockwaitTimeoutForLockedUpdateTest {

    private static final Logger logger = Logger.getLogger(LockwaitTimeoutForLockedUpdateTest.class);

    public static void main(String[] args) throws SQLException {
        logger.debug("开始---------------------------------------------------------");
        noMoreConnInThreadPool();
    }

    /**
     * 主要思路：用户线程执行一个更新语句来更新记录，而记录已经被数据库的另一个用户会话锁住。此时会出现等待排他锁超时
     */
    public static void noMoreConnInThreadPool() {
        new MyThread3().start();
    }
}
