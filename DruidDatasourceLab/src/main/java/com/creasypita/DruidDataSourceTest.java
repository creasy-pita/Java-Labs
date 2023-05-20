package com.creasypita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lujq on 5/20/2023.
 */
public class DruidDataSourceTest {

    public static void main(String[] args) throws SQLException {
        Connection connection = DruidUtil.getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from bt_user limit 2");
        ResultSet resultSet = ps.executeQuery();
        System.out.println(resultSet.toString());
        DruidUtil.close(connection,ps,resultSet);
    }
}
