package com.creasypita;

/**
 * Created by lujq on 8/12/2024.
 * 使用thread类中的statck属性获取方法栈
 */
public class MyThreadStackDemo {
    public static void main(String[] args) {
        methodA();
    }

    public static void methodA() {
        methodB();
    }

    public static void methodB() {
        printStackTrace();
    }

    public static void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            System.out.println(element);
        }
    }
}
