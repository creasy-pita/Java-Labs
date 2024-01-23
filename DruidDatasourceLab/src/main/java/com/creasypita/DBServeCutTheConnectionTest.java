package com.creasypita;


import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by lujq on 5/20/2023.
 */
public class DBServeCutTheConnectionTest {

    private static final Logger logger = Logger.getLogger(DBServeCutTheConnectionTest.class);

    public static void main(String[] args) throws SQLException {
        logger.debug("开始---------------------------------------------------------");
        testDBServeCutTheConnection();
    }

    /**
     * 测试数据库服务端主动掐掉连接，客户端这些连接与服务端通信发数据时报错的场景
     */
    public static void testDBServeCutTheConnection() {
        for (int i = 0; i < 10; i++) {
            new MyThread().start();
        }

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("---------------------------------------------------------");

        for (int i = 0; i < 10; i++) {
            new MyThread().start();
        }

    }

    public static void testRemove(){
//        Connection connection = DruidUtil.getConnection();
//        PreparedStatement ps = connection.prepareStatement("select * from bt_user limit 2");
//        ResultSet resultSet = ps.executeQuery();
//        System.out.println(resultSet.toString());
//        DruidUtil.close(connection,ps,resultSet);
        for (int i = 0; i < 35; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new MyThread().start();
            new MyThread().start();
            new MyThread2().start();
        }
    }
}
