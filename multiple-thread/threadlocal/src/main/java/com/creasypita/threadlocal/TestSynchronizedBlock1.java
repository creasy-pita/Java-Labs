package com.creasypita.threadlocal;

/**
 * Created by lujq on 10/17/2022.
 */
public class TestSynchronizedBlock1{
    public static void main(String args[]){
        SimpleLock obj = new SimpleLock();
        //only one object
        MyThread1 t1=new MyThread1(obj);
        MyThread2 t2=new MyThread2(obj);
        t1.start();
        t2.start();
    }
}
