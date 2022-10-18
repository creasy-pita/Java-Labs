package com.creasypita.threadLock;

/**
 * Created by lujq on 10/18/2022.
 */
public class AppService {
    private static final Object a = new Object();
    private static final Object b = new Object();

    public void Method1(){
        synchronized (a){
            System.out.println("method1 get a");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (b){
                System.out.println("method1 get b");
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void Method2(){
        synchronized (b){
            System.out.println("method2 get b");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (a){
                System.out.println("method2 get a");
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
