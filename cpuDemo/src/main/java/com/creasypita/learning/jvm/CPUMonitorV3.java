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
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

//        private double getCPUUsage() throws IOException, InterruptedException {
//            String command = "top -bn1 | grep 'Cpu(s)' | sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | awk '{print 100 - $1}'";
//            Process process = Runtime.getRuntime().exec(command);
//            process.waitFor();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line = reader.readLine();
//            reader.close();
//            return Double.parseDouble(line);
//        }

        private double getCPUUsage() throws IOException, InterruptedException {
            // 刷新两次top命令，取第二次刷新的数据
            String command = "top -bn2 | grep 'Cpu(s)' | sed -n '2p' | sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | awk '{print 100 - $1}'";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line = reader.readLine();
//            System.out.println("getCPUUsage:" + line);
//            reader.close();
//            return Double.parseDouble(line);



            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(command + ",命令执行结果：" + line);
                }

                while ((line = errorReader.readLine()) != null) {
                    // 输出标准错误（stderr），标记为错误信息
                    if (line.startsWith("[ERROR]")) {
                        System.out.println(command + "，命令执行错误："+line);
                    } else {
                        System.out.println(command + "，[WARNING]" + line);
                    }
                }

                // 等待命令执行完成
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("执行命令失败！");
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt(); // 如果是InterruptedException，则重新设置中断状态
                throw new RuntimeException(e);
            }
            return 0;
        }


        private void findAndDumpHighCPUThread() throws IOException, InterruptedException {
            String command = "top -H -b -n 1 -o %CPU";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("java")) {
                    System.out.println("Found Java Process:");
                    System.out.println(line);
                    found = true;
                    break;
                }
            }
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

