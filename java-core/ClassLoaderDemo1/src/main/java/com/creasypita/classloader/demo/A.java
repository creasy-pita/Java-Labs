package com.creasypita.classloader.demo;

/**
 * Created by lujq on 8/3/2023.
 */
public class A {
    public void doSomething(C c){
        System.out.println(c.getClass().getClassLoader());
        c.doSomething();
    }
}
