package com.creasypita.threadlocal;

/**
 * Created by lujq on 10/17/2022.
 */
public class SimpleLock {
    void printTable(int n){
        synchronized(this){
            //synchronized block
            for(int i=1;i<=50;i++) {
                System.out.println(n * i);
                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
//end of the method}
class MyThread1 extends Thread{
    SimpleLock t;
    MyThread1(SimpleLock t){
        this.t=t;
    }
    public void run(){
        t.printTable(5);
    }
}
class MyThread2 extends Thread{
    SimpleLock t;
    MyThread2(SimpleLock t){
        this.t=t;
    }
    public void run(){
        t.printTable(100);
    }
}

