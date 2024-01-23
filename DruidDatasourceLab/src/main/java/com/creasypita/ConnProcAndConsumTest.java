package com.creasypita;


import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by lujq on 5/20/2023.
 * 测试用户线程消费（获取）连接和生产线程生产连接
 */
public class ConnProcAndConsumTest {

    private static final Logger logger = Logger.getLogger(ConnProcAndConsumTest.class);

    public static void main(String[] args) throws SQLException {
        logger.debug("开始---------------------------------------------------------");
        testConnProcAndConsum();
    }

    /**
     *
     */
    public static void testConnProcAndConsum() {
        for (int i = 0; i < 2; i++) {
            new MyThread().start();
        }

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
