package com.creasypita.exception;

/**
 * Created by lujq on 8/15/2023.
 */
public class MyThreadWithException extends Thread {
    @Override
    public void run() {
        throw new RuntimeException("MyThreadWithException exception");
    }
}
