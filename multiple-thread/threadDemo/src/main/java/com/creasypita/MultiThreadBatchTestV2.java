package com.creasypita;

import java.text.ParseException;
import java.util.Date;

/*
批处理 m * n 个任务demo,通过m个线程分别处理n个任务；
线程中使用线程池来处理，每个任务通过 Thread.sleep(100) 来网络模拟演示
* */
public class MultiThreadBatchTestV2 implements Runnable {
    static int LoopNum = 10;//单个线程循环的任务次数
    static int ThreadNum = 100;//线程数
    static String IP = "192.168.51.152";//用配置文件则无效
    static final int MAX_THREAD = 10240*2;
    static int[] totalCount = new int[MAX_THREAD];
    static int[] errorCount = new int[MAX_THREAD];
    static java.lang.Thread[] th = new java.lang.Thread[MAX_THREAD];
    static int testItem = 1;
    static String functionName = null;

    public void run() {
        try {
            generalDataEnc();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * ip
     * 线程数
     * 每个线程循环跑业务的次数
     * @param args
     */
    public static void main(String[] args) {
        Date ed;
        int i = 0;
        String thNm = null;
        if (args.length >= 5) {
            IP = args[0].trim();
            ThreadNum = Integer.parseInt(args[1]);
            LoopNum = Integer.parseInt(args[2]);
            testItem = Integer.parseInt(args[3]);
        }

        System.out.printf("Starting Threads Test <%s> for [%d * %d] Threads*Loops \n",
                new Object[] { functionName, Integer.valueOf(ThreadNum), Integer.valueOf(LoopNum) });
        try {
            java.lang.Thread.sleep(100L);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        //创建本类实例
        MultiThreadBatchTestV2 test = new MultiThreadBatchTestV2();
        //创建线程
        for (i = 0; i < ThreadNum; i++) {
            thNm = String.format("%d", new Object[] { Integer.valueOf(i) });
            th[i] = new java.lang.Thread(test, thNm);
            th[i].setDaemon(true);
            th[i].start();
        }
        synchronized (test) {
            try {
                test.wait(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            test.notifyAll();
        }
        Date sd = new Date();
        //监测线程状态和性能
        while (true) {
            long l1 = 0L, l2 = 0L;
            int tThread = ThreadNum;
            //判断每个线程是否存活
            for (i = 0, tThread = ThreadNum; i < ThreadNum; i++) {
                if (!th[i].isAlive())
                    tThread--;
                l1 += totalCount[i];
                l2 += errorCount[i];
            }
            ed = new Date();
            long l3 = ed.getTime() - sd.getTime();
            if (l3 != 0L)
                System.out.printf("<%08d> s Passed, [%d] Threads Running, Total [%08d], Error [%08d] TPS [%08d]\n",
                        new Object[] { Long.valueOf(l3 / 1000L), Integer.valueOf(tThread), Long.valueOf(l1), Long.valueOf(l2), Long.valueOf(l1 * 1000L / l3) });
            //存活线程为0，退出程序
            if (tThread == 0)
                break;
            //每两秒检测（输出）一次
            synchronized (test) {
                try {
                    test.wait(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //线程结束，总输出
        long t = ed.getTime() - sd.getTime();
        int tCount = 0, eCount = 0;
        for (i = 0; i < ThreadNum; i++) {
            eCount += errorCount[i];
            tCount += totalCount[i];
        }
        System.out.println(java.lang.Thread.currentThread().getName() + "  Completed. timepassed <" + t + "> ms TPS <" + ((tCount - eCount) / t / 1000L) + ">.");
    }

    //计时器用的参数
    private volatile boolean keepRunning = true;
    public void generalDataEnc() throws ParseException {
        int i, threadNo;
        //获取线程名
        threadNo = Integer.parseInt(java.lang.Thread.currentThread().getName());
        totalCount[threadNo] = 0;
        // 等待主线程唤醒  this 也就是 MultiThreadBatchTestV2 test
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        boolean ret = false;
        //循环跑业务
        for (i = 0; i < LoopNum; i++) {
            try {
                // 模拟调用业务接口的耗时 100ms
                System.out.println("thread --> "+   Integer.parseInt(java.lang.Thread.currentThread().getName()) + " Loop<" + i + "> start.");
                Thread.sleep(100);
            } catch (Exception e) {
                errorCount[threadNo]++;
                continue;
            } finally {
                totalCount[threadNo]++;
            }
            if (ret == true ) {
                errorCount[threadNo]++;
            }
        }
        //输出线程接数标识
        System.out.println(java.lang.Thread.currentThread().getName() + " Loop<" + i + "> Completed.");
        return;
    }
}

