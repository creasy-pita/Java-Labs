package com.creasypita.threadLock;

/**
 * Created by lujq on 10/18/2022.
 */
public class SimpleDeadLock {
    public static void main(String[] args) {
        new MyThread1().start();
        new MyThread2().start();
    }
}

