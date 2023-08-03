package com.creasypita.classloader.demo;

import com.creasypita.MyWebAppLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by lujq on 8/3/2023.
 */
public class DemoTest {
    public static void main(String[] args) throws MalformedURLException, NoSuchMethodException {
        MyWebAppLoader loader1 = new MyWebAppLoader("E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoaderDemoA\\src\\main\\resources\\demo2\\");
        try {
            Class<?> clazzB = loader1.loadClass("com.creasypita.classloader.demo.B");
            Object objB = clazzB.newInstance();
            System.out.println("objB classloader1:" + objB.getClass().getClassLoader());
//            Method method1 = clazzB.getDeclaredMethod("doSomething1", null);
//            Method method2 = clazzB.getDeclaredMethod("doSomething2", null);
            Method method3 = clazzB.getDeclaredMethod("doSomething3", null);
//            method1.invoke(objB, null);
//            method2.invoke(objB, null);
            method3.invoke(objB, null);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
