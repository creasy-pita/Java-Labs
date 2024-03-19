package com.creasypita.learning.jvm;

/**
 * Created by lujq on 3/19/2024.
 */
public class CPUSimulator {
    public static void main(String[] args) {
        long duration = 600000; // 模拟的持续时间（毫秒），这里设定为1分钟
        long endTime = System.currentTimeMillis() + duration;

        while (System.currentTimeMillis() < endTime) {
            // 在结束时间之前不断执行一些计算，以模拟CPU负载
            long x = 0;
            for (int i = 0; i < 1000000; i++) {
                x = x + i;
            }
        }
    }
}

