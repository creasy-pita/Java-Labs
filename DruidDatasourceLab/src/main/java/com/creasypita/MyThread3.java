package com.creasypita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lujq on 5/20/2023.
 * 用于执行update表记录的任务线程
 */
public class MyThread3 extends Thread{
    public void run(){
        Connection connection = null;
        try {
            connection = DruidUtil.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("update bt_user set staff_name='系统管理员1' where username='admin'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ResultSet resultSet = null;
        try {
            System.out.println(Thread.currentThread() + "开始执行sql...");
            System.out.println(ps.execute());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("连接使用5秒");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(resultSet.toString());
        DruidUtil.close(connection,ps,resultSet);
    }
}
