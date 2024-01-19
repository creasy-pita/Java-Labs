package com.creasypita;

import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by lujq on 1/19/2024.
 * 模拟连接池满，最大等待时间内无法获取连接的场景
 */
public class NoMoreConnInThreadPoolTest {

    private static final Logger logger = Logger.getLogger(NoMoreConnInThreadPoolTest.class);

    public static void main(String[] args) throws SQLException {
        logger.debug("开始---------------------------------------------------------");
        noMoreConnInThreadPool();
    }

    /**
     * 主要思路
     * 数据库连接池设置 初始连接数和最大连接数设置为20, 用户线程获取连接的最大等待时间为5s  initialSize=20 maxActive=20
     * 使用MyThread去获取连接执行sql,显示让执行过程花费5s
     * 方法种循环25次，发起25个连接请求，后边5个请求大概都会提示连接池满，获取连接超时的信息
     */
    public static void noMoreConnInThreadPool() {
        for (int i = 0; i < 25; i++) {
            new MyThread().start();
        }
    }
}
