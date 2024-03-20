//package com.creasypita.learning.jvm;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
//
//public class CPUMonitor {
//    private static final double CPU_THRESHOLD = 0.6; // CPU 使用率阈值
//
//    public static void main(String[] args) {
//        // 创建一个定时任务，每隔一段时间检查一次 CPU 使用率
//        Runnable task = () -> {
//            while (true) {
//                try {
//                    Thread.sleep(5000); // 每隔5秒检查一次 CPU 使用率
//                    double cpuUsage = getProcessCpuUsage();
//                    System.out.println("Current CPU Usage: " + cpuUsage);
//
//                    if (cpuUsage > CPU_THRESHOLD) {
//                        System.out.println("CPU usage exceeds threshold. Dumping thread stack traces...");
//
//                        // 获取占用 CPU 最高的 Java 进程的线程转储
//                        String jstackCommand = "jstack " + findHighestCpuJavaProcess();
//                        executeCommand(jstackCommand);
//
//                        System.out.println("Thread dump completed.");
//                    }
//                } catch (InterruptedException | IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        Thread thread = new Thread(task);
//        thread.start();
//    }
//
//    // 获取当前进程的 CPU 使用率
//    private static double getProcessCpuUsage() {
//        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
//        double cpuUsage = osBean.getSystemLoadAverage();
//        return cpuUsage;
//    }
//
//    // 执行系统命令，并输出结果
//    private static void executeCommand(String command) throws IOException {
//        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
//    }
//
//    // 找到占用 CPU 最高的 Java 进程的 PID
//    private static String findHighestCpuJavaProcess() throws IOException {
//        ProcessBuilder processBuilder = new ProcessBuilder("top", "-b", "-n", "1");
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//
//        StringBuilder output = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//        }
//
//        // 解析 top 输出，找到占用 CPU 最高的 Java 进程的 PID
//        String[] lines = output.toString().split("\n");
//        String javaProcessPID = null;
//        double highestCPUUsage = 0;
//        for (String line : lines) {
//            if (line.contains("java") && line.contains("PID")) {
//                String[] parts = line.trim().split("\\s+");
//                String pid = parts[0];
//                double cpuUsage = Double.parseDouble(parts[8]);
//                if (cpuUsage > highestCPUUsage) {
//                    highestCPUUsage = cpuUsage;
//                    javaProcessPID = pid;
//                }
//            }
//        }
//
//        return javaProcessPID != null ? "jstack " + javaProcessPID : null;
//    }
//}
//
