package com.creasypita;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lujq on 9/10/2025.
 */
public class MultiThreadBatchTest {

    /*
    批处理 m * n 个任务demo,通过m个线程分别处理n个任务；
    线程中使用线程池来处理，每个任务通过 Thread.sleep(100) 来网络模拟演示
    * */

    private static int threadNum = 1000;
    private static int taskNum = 10;

    private static ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(500, 500, 30000, TimeUnit.SECONDS, new LinkedBlockingQueue(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void main(String[] args) {
//        CompletableFuture_lamandaRunable_ThreadTest();
        CompletableFuture_MyRunable_ThreadTest();
        CountDownLatch_MyRunable_ThreadTest();
    }

    public static void CompletableFuture_lamandaRunable_ThreadTest(){
        long start = System.currentTimeMillis();
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (int i = 0; i < threadNum; i++) {
            futureList.add( CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < taskNum; j++) {
                        if (j == 9) {
                            System.out.println("处理第" + j + "个任务");
                        }
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, executor));
        }
        CompletableFuture.allOf((CompletableFuture[])futureList.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("开始时间：" + start);
        System.out.println("开始时间：" + end);
        System.out.println(String.format("%d线程分别处理%d个任务，共%d个任务耗时%dms", threadNum, taskNum, threadNum * taskNum, end - start));
    }

    public static void CompletableFuture_MyRunable_ThreadTest(){
        long start = System.currentTimeMillis();
        System.out.println("----->开始时间：" + start);
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (int i = 0; i < threadNum; i++) {
            futureList.add( CompletableFuture.runAsync(new MyRunable(i, taskNum), executor));
        }
        // 通过CompletableFuture 阻塞等待全部线程完成后再放行
        // 这里每个线程的任务的耗时基本相同，所以基本不会存在完成快的线程等待完成慢的线程的问题
        CompletableFuture.allOf((CompletableFuture[])futureList.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("----->结束时间：" + end);
        System.out.println(String.format("%d线程分别处理%d个任务，共%d个任务耗时%dms", threadNum, taskNum, threadNum * taskNum, end - start));
    }

    public static void CountDownLatch_MyRunable_ThreadTest() {
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        System.out.println("----->开始时间：" + start);

        for (int i = 0; i < threadNum; i++) {
             CompletableFuture.runAsync(new MyCountDownLatchRunable(i, taskNum, countDownLatch), executor);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("----->结束时间：" + end);
        System.out.println(String.format("%d线程分别处理%d个任务，共%d个任务耗时%dms", threadNum, taskNum, threadNum * taskNum, end - start));
    }

}

class MyRunable implements Runnable{

    private int threadNum ;
    private int taskNum ;

    public MyRunable(int threadNum, int taskNum){
        this.threadNum = threadNum;
        this.taskNum = taskNum;
    }

    @Override
    public void run() {
        try {
            for (int j = 0; j < taskNum; j++) {
//                if (j == 9 && threadNum%100 == 0) {
                if (j == 9) {
                    System.out.println("线程【" + threadNum +"】处理第【" + j + "】个任务");
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MyCountDownLatchRunable implements Runnable{

    private int threadNum ;
    private int taskNum ;
    private CountDownLatch countDownLatch;

    public MyCountDownLatchRunable(int threadNum, int taskNum, CountDownLatch countDownLatch){
        this.threadNum = threadNum;
        this.taskNum = taskNum;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            for (int j = 0; j < taskNum; j++) {
//                if (j == 9 && threadNum%100 == 0) {
                if (j == 9) {
                    System.out.println("线程【" + threadNum +"】处理第【" + j + "】个任务");
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.countDownLatch.countDown();
        }
    }
}
