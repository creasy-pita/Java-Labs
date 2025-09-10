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

    private static int m = 1000;
    private static int n = 10;

    private static ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(50, 500, 30000, TimeUnit.SECONDS, new LinkedBlockingQueue(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (int i = 0; i < m; i++) {
            futureList.add( CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < n; j++) {
                        if (j==9) {
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
        System.out.println(String.format("%d线程分别处理%d个任务，共%d个任务耗时%dms", m , n, m * n, end - start));

    }
}
