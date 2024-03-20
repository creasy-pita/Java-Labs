package com.creasypita.learning.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class CPUMonitorV3 {

    private static final double THRESHOLD = 12.0; // CPU使用率阈值
    private static final long DELAY = 1000; // 延迟1秒开始执行
    private static final long INTERVAL = 10000; // 每隔5秒检测一次CPU使用率

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new CPUMonitorTask(), DELAY, INTERVAL);
    }

    static class CPUMonitorTask extends TimerTask {
        @Override
        public void run() {
            try {
                double cpuUsage = getCPUUsage();
                System.out.println("Current CPU Usage: " + cpuUsage + "%");

                if (cpuUsage > THRESHOLD) {
                    System.out.println("CPU Usage exceeds " + THRESHOLD + "%, finding process and thread...");
                    findAndDumpHighCPUThread();
                    System.out.println("run() 1");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        private double getCPUUsage() throws IOException, InterruptedException {
            // 方式1 Process process = Runtime.getRuntime().exec(jstackCommand);
            // 方式2 Process process = processBuilder.start()
            // 方式1 不支持 shell 的管道符和重定向机制 方式2可以
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "top -bn2 | grep 'Cpu(s)' | sed -n '2p' | sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | awk '{print 100 - $1}'");
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            System.out.println("cpuusge:" + line);
            reader.close();
            return Double.parseDouble(line);
        }

        private void findAndDumpHighCPUThread() throws IOException, InterruptedException {
            // top 按-o 选项中的cpu从高到低排序
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "top -H -b -n 1 -o %CPU");
            Process process = processBuilder.start();

            System.out.println("1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println("2");
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("java")) {
                    System.out.println("Found Java Process:");
                    System.out.println(line);
                    found = true;
                    break;
                }
            }
            // 等待进程执行完成，并获取返回值
//            int exitCode = process.waitFor();
//            System.out.println("Exit code: " + exitCode);
            process.destroy();
            reader.close();

            if (found) {
                String[] parts = line.trim().split("\\s+");
                String pid = parts[0];
                String jstackCommand = "jstack " + pid;
                System.out.println("Begin Dumping threads of the process  with PID: " + pid);
                Process jstackProcess = Runtime.getRuntime().exec(jstackCommand);
                jstackProcess.waitFor();
                BufferedReader jstackReader = new BufferedReader(new InputStreamReader(jstackProcess.getInputStream()));
                String jstackLine;
                while ((jstackLine = jstackReader.readLine()) != null) {
                    System.out.println(jstackLine);
                }
                jstackReader.close();
                System.out.println("End Dumping threads of the process end with PID: " + pid);
                Thread.sleep(10000);
            }
        }
    }
}

