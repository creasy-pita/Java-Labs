package com.creasypita.exception;

/**
 * Created by lujq on 8/15/2023.
 * main方法中不直接输出子线程任务执行中抛出的异常
 * 总结：子线程任务执行中抛出的异常，会抛到父线程
 *  * 内层AA抛出异常如果没有catch,会抛出到出口main方法，jvm中main方法结束时走的是异常出口，而不是正常的方法出口。
 *  * 此时会在控制台打印异常，不会被忽略。
 *  * 延申
 *  * 如果是子线程中的异常，也是类似main线程的情况，会走异常出口，并打印
 */
public class NoPrintOutExceptionFromSubThreadInMain {
    public static void main(String[] args) {
        try {
            new MyThreadWithException().start();
        } catch (Exception e) {
            throw e;
        }
    }
}
