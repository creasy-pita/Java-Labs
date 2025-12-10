package com.creasypita.jdbc;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.*;
import java.util.UUID;

/**
 * Created by lujq on 12/9/2025.
 */
public class PreparedStatementTest {

//    static final String DB_URL = "jdbc:mysql://192.168.4.236:3306/platform?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2b8&allowMultiQueries=true";
//    static final String USER = "root";
//    static final String PASS = "root";

        static final String DB_URL = "jdbc:postgresql://192.168.13.16:54321/bdcpz?prepareThreshold=2&currentSchema=platform&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
    static final String USER = "system";
    static final String PASS = "AyNTUuMC4wL.jAK279I";
    static final String QUERY = "select * from zpt_bizdef a where a.bizdef_guid=?";


//    static final String DB_URL = "jdbc:postgresql://10.44.242.33:54321/zwzt?currentSchema=apolloconfigdb&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
//    static final String USER = "system";
//    static final String PASS = "Jhfgw0219@";
//    static final String QUERY = "SELECT * FROM app";

    //system，密码Jhfgw0219@，端口为54321

//    static final String DB_URL = "jdbc:oracle:thin:@192.168.11.42:1521:orcl";
//    static final String USER = "platform";
//    static final String PASS = "PLATFORMzjzs1234";
//
//    static final String DB_URL = "jdbc:oracle:thin:@192.168.100.30:1521:orcl";
//    static final String USER = "bdcdj";
//    static final String PASS = "bdcdj";

    //    static final String DRIVERCLASS = "oracle.jdbc.driver.OracleDriver";
    static final String DRIVERCLASS = "org.postgresql.Driver";
//    static final String DRIVERCLASS = "com.mysql.jdbc.Driver";
    static final DruidDataSource ds = new DruidDataSource() ;



    static {
        ds.setUrl(DB_URL);
        ds.setUsername(USER);
        ds.setPassword(PASS);
        ds.setDriverClassName(DRIVERCLASS);
        ds.setMinIdle(10);
        ds.setInitialSize(10);
        ds.setMaxActive(20);
//        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }


    public static void main(String[] args) throws InterruptedException {
        query();

    }

    public static void query(){
        try(Connection conn = ds.getConnection()) {
            Thread.sleep(1);
//            第一次
            try(PreparedStatement stmt = conn.prepareStatement(QUERY)){
                stmt.setString(1,  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                stmt.executeQuery();
            }
//            第2,3次
            for (int j = 0; j < 2; j++) {
                try(PreparedStatement stmt = conn.prepareStatement(QUERY)){
                    stmt.setString(1, j + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    stmt.executeQuery();
                }
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}



