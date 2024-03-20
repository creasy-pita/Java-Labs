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

        private double getCPUUsage()  {
            // 方式1 Process process = Runtime.getRuntime().exec(jstackCommand);
            // 方式2 Process process = processBuilder.start()
            // 方式1 不支持 shell 的管道符和重定向机制 方式2可以
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "top -bn2 | grep 'Cpu(s)' | sed -n '2p' | sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | awk '{print 100 - $1}'");
            String line;
            try {
                Process process = processBuilder.start();
                process.waitFor();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    line = reader.readLine();
                } catch ( IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Double.parseDouble(line);
        }

        private void findAndDumpHighCPUThread() throws IOException, InterruptedException {
            // top 按-o 选项中的cpu从高到低排序
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "top -H -b -n 1 -o %CPU");
            Process process = processBuilder.start();
            String hitLine;
            boolean found;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                hitLine = null;
                found = false;

                while ((line = reader.readLine()) != null) {
                    if (!found && line.contains("java")) {
                        hitLine = line;
                        System.out.println("Found Java Process:");
                        System.out.println(line);
                        found = true;
                    }
                }
                // 等待进程执行完成，并获取返回值
                int exitCode = process.waitFor();
                System.out.println("Exit code: " + exitCode);
            } catch (IOException e){
                throw new RuntimeException(e);
            }

            if (found) {
                String[] parts = hitLine.trim().split("\\s+");
                String pid = parts[0];
                String jstackCommand = "jstack " + pid;
                System.out.println("Begin Dumping threads of the process  with PID: " + pid);
                Process jstackProcess = Runtime.getRuntime().exec(jstackCommand);
                try (BufferedReader jstackReader = new BufferedReader(new InputStreamReader(jstackProcess.getInputStream()))) {
                    String jstackLine;
                    while ((jstackLine = jstackReader.readLine()) != null) {
                        System.out.println(jstackLine);
                    }
                }
                // 等待进程执行完成，并获取返回值
                int exitCode = jstackProcess.waitFor();
                System.out.println("Exit code: " + exitCode);
                System.out.println("End Dumping threads of the process end with PID: " + pid);
                Thread.sleep(10000);
            }
        }
    }
}

