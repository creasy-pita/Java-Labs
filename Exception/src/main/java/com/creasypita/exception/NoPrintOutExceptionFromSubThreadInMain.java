package com.creasypita.exception;

/**
 * Created by lujq on 8/15/2023.
 * main方法中不直接输出子线程任务执行中抛出的异常
 * 总结：子线程任务执行中抛出的异常，会跑到父线程
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
