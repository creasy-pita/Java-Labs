package com.creasypita.jdbc;

import java.sql.*;

/**
 * Created by lujq on 6/30/2022.
 */
public class SimpleQuery {
//    static final String DB_URL = "jdbc:postgresql://192.168.100.66:5432/zwzt?currentSchema=businessdb&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
    static final String DB_URL = "jdbc:postgresql://192.168.100.66:5432/zwzt?currentSchema=apolloconfigdbold&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
    static final String USER = "postgres";
    static final String PASS = "postgres";
    static final String QUERY = "SELECT * FROM app";

    public static void main(String[] args) {
        // Open a connection
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
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

        } catch (SQLException  e) {
            e.printStackTrace();
        }
    }


}

