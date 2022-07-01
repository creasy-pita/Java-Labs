package com.creasypita.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by lujq on 6/30/2022.
 */
public class DruidDataSourceTest {

    static final String DB_URL = "jdbc:postgresql://192.168.100.66:5432/zwzt?currentSchema=apolloconfigdbold&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
    static final String USER = "postgres";
    static final String PASS = "postgres";
    static final String QUERY = "SELECT * FROM app";
    static final DruidDataSource ds = new DruidDataSource() ;

static {
    ds.setUrl(DB_URL);
    ds.setUsername(USER);
    ds.setPassword(PASS);

    ds.setMinIdle(10);
    ds.setInitialSize(10);
    ds.setMaxActive(20);
//        ds.setMaxIdle(10);
    ds.setMaxOpenPreparedStatements(100);
}


    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    query();
                }
            }).start();
        }
        Thread.sleep(20000);
    }

    public static void query(){
        try(Connection conn = ds.getConnection()) {
            Thread.sleep(5000);
            try(Statement stmt = conn.createStatement()) {
                try(ResultSet rs = stmt.executeQuery(QUERY)) {

                    // Extract data from result set
                    while (rs.next()) {
                        // Retrieve by column name
                        System.out.println("1:" + rs.getString(1));
//                System.out.print("ID: " + rs.getInt("user_id"));
//                System.out.print(", NAME: " + rs.getInt("user_name"));
                    }
                }
            }

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
