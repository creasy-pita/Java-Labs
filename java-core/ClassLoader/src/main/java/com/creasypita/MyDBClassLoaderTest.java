package com.creasypita;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.util.Properties;

/**
 * Created by lujq on 7/1/2023.
 */
public class MyDBClassLoaderTest {
    static final String USER = "root";
    static final String PASS = "123456";
    static final String DB_URL = "jdbc:mysql://192.168.100.66:3306/platform?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2b8&allowMultiQueries=true";
    static final String MYSQL5X_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String MYSQL8X_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) throws MalformedURLException, NoSuchMethodException {
        MyWebAppLoader a = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\a\\");
        try {
            //注册jdbc驱动
            Class<?> driverClazz = a.loadClass(MYSQL5X_JDBC_DRIVER);
            Driver driver = (Driver) driverClazz.newInstance();
//            Object obj =  driverClazz.newInstance();
            System.out.println(driver.getClass().getClassLoader());
//            System.out.println(obj.getClass().getClassLoader());
            Method connectMethod = driverClazz.getMethod("connect", String.class, Properties.class);
            Properties properties = new Properties();
            properties.put("user", USER);
            properties.put("password", PASS);
            //获取连接
            Object connObj = connectMethod.invoke(driver, DB_URL, properties);
//            //输出连接类的classloader
            System.out.println(connObj.getClass().getClassLoader());

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public static void main1(String[] args) throws MalformedURLException, NoSuchMethodException {
        MyWebAppLoader a = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\a\\");
        try {
            //注册jdbc驱动
            Class<?> driverClazz = a.loadClass(MYSQL5X_JDBC_DRIVER);
            Class<?> drivermanagerClazz = a.loadClass("java.sql.DriverManager");
            //获取连接
            Method getConnection = drivermanagerClazz.getMethod("getConnection", String.class, String.class, String.class);
            Object connObj = getConnection.invoke(null, DB_URL, USER, PASS);
            //输出连接类的classloader
            System.out.println(connObj.getClass().getClassLoader());

        } catch (ClassNotFoundException |  IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }


        MyWebAppLoader b = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\b\\");
        try {
            //注册jdbc驱动
            Class<?> driverClazz = b.loadClass(MYSQL8X_JDBC_DRIVER);
            Class<?> drivermanagerClazz = b.loadClass("java.sql.DriverManager");
            //获取连接
            Method getConnection = drivermanagerClazz.getMethod("getConnection", String.class, String.class, String.class);
            Object connObj = getConnection.invoke(null, DB_URL, USER, PASS);
            //输出连接类的classloader
            System.out.println(connObj.getClass().getClassLoader());

        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
