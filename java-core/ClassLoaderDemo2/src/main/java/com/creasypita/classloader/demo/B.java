package com.creasypita.classloader.demo;

import com.creasypita.MyWebAppLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by lujq on 8/3/2023.
 */
public class B {
    /**
     * A和C的实例使用相同的classloader创建，所以正常执行
     * @throws MalformedURLException
     * @throws NoSuchMethodException
     */
    public void doSomething1() throws MalformedURLException, NoSuchMethodException {
        // ajar中有类A, bjar中有类B
        String aJarpath = "E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoaderDemoA\\src\\main\\resources\\demo1\\";
        String bJarpath = "E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoaderDemoA\\src\\main\\resources\\demo2\\";
        MyWebAppLoader loader1 = new MyWebAppLoader(aJarpath);
        MyWebAppLoader loader2 = new MyWebAppLoader(bJarpath);
        try {
            Class<?> clazzA = loader1.loadClass("com.creasypita.classloader.demo.A");
            Object objA = clazzA.newInstance();
            System.out.println("obj classloader:" + objA.getClass().getClassLoader());

            Class<?> clazzC = loader2.loadClass("com.creasypita.classloader.demo.C");
            Object objC = clazzC.newInstance();
            System.out.println("obj classloader:" + objC.getClass().getClassLoader());

            //A实例调用其实例方法，并传入C的实例
            Method method = clazzA.getDeclaredMethod("doSomething", clazzC);
            method.invoke(objA, objC);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * A和C的实例使用不同的classloader创建，所以会报错
     * @throws MalformedURLException
     * @throws NoSuchMethodException
     */
    public void doSomething2() throws MalformedURLException, NoSuchMethodException {
        // ajar中有类A, bjar中有类B
        String aJarpath = "E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoaderDemoA\\src\\main\\resources\\demo1\\";
        MyWebAppLoader loader1 = new MyWebAppLoader(aJarpath);
        try {
            Class<?> clazzA = loader1.loadClass("com.creasypita.classloader.demo.A");
            Object objA = clazzA.newInstance();
            System.out.println("obj classloader:" + objA.getClass().getClassLoader());

            Class<?> clazzC = this.getClass().getClassLoader().loadClass("com.creasypita.classloader.demo.C");
            Object objC = clazzC.newInstance();
            System.out.println("obj classloader:" + objC.getClass().getClassLoader());

            //A实例调用其实例方法，并传入C的实例
            Method method = clazzA.getDeclaredMethod("doSomething", clazzC);
            method.invoke(objA, objC);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * A和C的实例使用相同的classloader创建，所以正常执行
     * @throws MalformedURLException
     * @throws NoSuchMethodException
     */
    public void doSomething3() throws MalformedURLException, NoSuchMethodException {
        // ajar中有类A, bjar中有类B
        String aJarpath = "E:\\work\\myproject\\java\\Java-Labs\\java-core\\ClassLoaderDemoA\\src\\main\\resources\\demo1\\";
        MyWebAppLoader loader1 = new MyWebAppLoader(aJarpath);
        try {
            Class<?> clazzA = loader1.loadClass("com.creasypita.classloader.demo.A");
            Object objA = clazzA.newInstance();
            System.out.println("obj classloader:" + objA.getClass().getClassLoader());

            Class<?> clazzC = loader1.loadClass("com.creasypita.classloader.demo.C");
            Object objC = clazzC.newInstance();
            System.out.println("obj classloader:" + objC.getClass().getClassLoader());

            //A实例调用其实例方法，并传入C的实例
            Method method = clazzA.getDeclaredMethod("doSomething", clazzC);
            method.invoke(objA, objC);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
