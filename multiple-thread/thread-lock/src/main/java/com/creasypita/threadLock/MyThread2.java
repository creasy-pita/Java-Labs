package com.creasypita.threadLock;

/**
 * Created by lujq on 10/18/2022.
 */
public class MyThread2 extends Thread{
    public void run(){
        new AppService().Method2();
    }
}
