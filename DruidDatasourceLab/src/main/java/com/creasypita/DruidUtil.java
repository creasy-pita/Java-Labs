package com.creasypita;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.util.JdbcUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by lujq on 5/20/2023.
 */
public class DruidUtil {
    //连接池
    static DataSource dataSource;
    //读取配置信息的io
    static Properties properties = new Properties();
    static {
        //JDBCUtil.class.getClassLoader()是获得JDBCUtil类的类加载器
        //返回一个读取指定资源的输入流
        InputStream is = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
        try {
            properties.load(is);
            dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //获取连接
    public static Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
//        System.out.println("当前连接池活跃连接数为" +  ((DruidDataSource) dataSource).getActiveCount());
//        System.out.println("当前连接池空闲连接数为" +   ((DruidDataSource) dataSource).getPoolingCount());
        return connection;
    }
    //释放资源
    public static void close(Connection conn, Statement ps, ResultSet rs){
        try {
            if (rs != null){
                rs.close();
                rs = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (ps != null){
                ps.close();
                ps = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null){
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread() + "当前连接池活跃连接数为" +  ((DruidDataSource) dataSource).getActiveCount());
        System.out.println(Thread.currentThread() + "当前连接池空闲连接数为" +   ((DruidDataSource) dataSource).getPoolingCount());
    }

}
