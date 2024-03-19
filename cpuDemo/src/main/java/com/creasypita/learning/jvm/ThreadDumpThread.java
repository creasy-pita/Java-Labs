package com.creasypita.learning.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Created by lujq on 3/19/2024.
 */
public class ThreadDumpThread extends Thread{
    private volatile boolean running = true;
    private final long intervalMillis;

    public ThreadDumpThread(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        while (running) {
            if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {
                long[] threadIds = threadMXBean.getAllThreadIds();
                System.out.println("Generating thread dump...");
                for (long threadId : threadIds) {
                    ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
                    System.out.println(threadInfo.toString());
                }
                System.out.println("Thread dump generated successfully.");
            } else {
                System.err.println("Thread dump generation is not supported.");
            }

            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        running = false;
    }

    public static void main(String[] args) {
        // 创建并启动线程转储生成的异步线程
        ThreadDumpThread threadDumpThread = new ThreadDumpThread(5000); // 设置线程转储生成的间隔为5秒
        threadDumpThread.start();

        // 程序其他逻辑...
        long duration = 60000; // 模拟的持续时间（毫秒），这里设定为1分钟
        long endTime = System.currentTimeMillis() + duration;

        while (System.currentTimeMillis() < endTime) {
            // 在结束时间之前不断执行一些计算，以模拟CPU负载
            long x = 0;
            for (int i = 0; i < 1000000; i++) {
                x = x + i;
            }
        }
        threadDumpThread.stopThread();
    }
}
