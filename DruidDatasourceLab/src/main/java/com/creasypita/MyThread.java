package com.creasypita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lujq on 5/20/2023.
 */
public class MyThread extends Thread{
    public void run(){
        Connection connection = null;
        try {
            connection = DruidUtil.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("select * from bt_user limit 2");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ResultSet resultSet = null;
        try {
            resultSet = ps.executeQuery();
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
