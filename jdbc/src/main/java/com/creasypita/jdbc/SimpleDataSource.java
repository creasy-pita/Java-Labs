package com.creasypita.jdbc;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by lujq on 6/30/2022.
 */
public class SimpleDataSource {

    static final String DB_URL = "jdbc:postgresql://192.168.100.66:5432/zwzt?currentSchema=apolloconfigdbold&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
    static final String USER = "postgres";
    static final String PASS = "postgres";
    static final String QUERY = "SELECT * FROM app";

    public static void main(String[] args) {
        BasicDataSource ds = getDataSource();
        try(Connection conn = ds.getConnection()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static BasicDataSource getDataSource()
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(DB_URL);
        ds.setUsername(USER);
        ds.setPassword(PASS);

        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
        return ds;
    }

}
