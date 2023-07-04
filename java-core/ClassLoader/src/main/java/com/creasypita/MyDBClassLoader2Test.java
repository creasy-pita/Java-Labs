package com.creasypita;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by lujq on 7/1/2023.
 */
public class MyDBClassLoader2Test {
    static final String USER = "root";
    static final String PASS = "123456";
    static final String DB_URL = "jdbc:mysql://192.168.100.66:3306/platform?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2b8&allowMultiQueries=true";
    static final String MYSQL5X_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String MYSQL8X_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) throws MalformedURLException, NoSuchMethodException {



        try {
            /*
            借助MyWebAppLoader 实例化MyWebApp2Loader
            再借助MyWebApp2Loader走DriverManager.getconnection.
            这样 getconnection中的caller.getClassLoader就会是MyWebAppLoader了
            */
            MyWebAppLoader b = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\b\\");
            Class<?> myWebApp2LoaderClazz = b.loadClass("com.creasypita.MyWebApp2Loader");
            Constructor<?> constructor = myWebApp2LoaderClazz.getConstructor(String.class);
            MyWebApp2Loader myWebAppL2oader = (MyWebApp2Loader)constructor.newInstance("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\a\\");
            Class<?> drivermanagerClazz = myWebAppL2oader.loadClass("java.sql.DriverManager");

            //获取连接
            Method getConnection = drivermanagerClazz.getMethod("getConnection", String.class, String.class, String.class);
            Object connObj = getConnection.invoke(null, DB_URL, USER, PASS);
            //输出连接类的classloader
            System.out.println(connObj.getClass().getClassLoader());

        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
