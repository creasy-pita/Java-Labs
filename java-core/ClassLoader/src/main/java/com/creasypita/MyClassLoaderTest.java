package com.creasypita;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by lujq on 7/1/2023.
 */
public class MyClassLoaderTest {
    public static void main(String[] args) throws MalformedURLException, NoSuchMethodException {
        MyWebAppLoader a = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\a\\");
        try {
//            Class<?> clazz = a.loadClass("dm.jdbc.driver.DmDriver");
//            Object obj = clazz.newInstance();
//            Method method = clazz.getDeclaredMethod("do_connect", null);
//            method.invoke(obj, null);
            Class<?> clazz = a.loadClass("com.creasypita.Person");
            Object obj = clazz.newInstance();
            System.out.println("obj classloader:" + obj.getClass().getClassLoader());
            Method method = clazz.getDeclaredMethod("doSomething", null);
            method.invoke(obj, null);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


        MyWebAppLoader b = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoader\\src\\main\\resources\\b\\");
        try {
//            Class<?> clazz = a.loadClass("dm.jdbc.driver.DmDriver");
//            Object obj = clazz.newInstance();
//            Method method = clazz.getDeclaredMethod("do_connect", null);
//            method.invoke(obj, null);
            Class<?> clazz = b.loadClass("com.creasypita.Person");
            Object obj = clazz.newInstance();
            Method method = clazz.getDeclaredMethod("doSomething", null);
            method.invoke(obj, null);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

}
