package com.creasypita.io;

import java.io.*;
import java.util.*;

public class DiagnosticObjectInputStream extends ObjectInputStream {
    // 当前遍历深度
    private int currentDepth = 0;

    // 最大允许深度（用于诊断）
    private final int maxDiagnosticDepth;

    // 当前遍历路径
    private final Deque<String> currentPath = new ArrayDeque<>();

    // 已处理对象引用表
    private final Map<Integer, String> objectHandles = new HashMap<>();

    // 对象计数
    private int objectCount = 0;

    // 用于跟踪内部handle的计数器
    private int nextHandle = baseWireHandle;
    private static final int baseWireHandle = 0x7e0000; // 与JDK内部对齐

    public DiagnosticObjectInputStream(InputStream in) throws IOException {
        this(in, Integer.MAX_VALUE);
    }

    public DiagnosticObjectInputStream(InputStream in, int maxDiagnosticDepth) throws IOException {
        super(in);
        this.maxDiagnosticDepth = maxDiagnosticDepth;
        enableResolveObject(true);
    }

    // 补充缺失的nextHandle方法
    private int nextHandle() {
        return nextHandle++;
    }

    @Override
    public Object readObjectOverride() throws IOException, ClassNotFoundException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 增加对象计数
        objectCount++;

        // 获取下一个可用handle（模拟JDK内部机制）
        int handle = nextHandle();

        currentDepth++;

        try {
            // 打印深度信息
            if (currentDepth > maxDiagnosticDepth) {
                throw new DepthLimitExceededException("深度超过阈值: " + maxDiagnosticDepth +
                        "\n当前路径: " + getCurrentPath());
            }

            if (currentDepth % 100 == 0 || objectCount % 1000 == 0) {
                printDiagnosticInfo("处理中", startTime);
            }
            // 实际读取对象
            Object obj = super.readObject();
            currentPath.push(obj.getClass().getName() + "@" + handle);

            // 记录对象引用
            objectHandles.put(handle, getCurrentPath());
            return obj;
        } finally {
            // 清理路径
            currentPath.pop();
            currentDepth--;

            // 打印完成信息
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 50 || currentDepth == 0) {
                printDiagnosticInfo("完成读取", startTime);
            }
        }
    }

    public Object readObjectOverride2() throws IOException, ClassNotFoundException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 增加对象计数
        objectCount++;

        // 获取下一个可用handle（模拟JDK内部机制）
        int handle = nextHandle();
        ObjectStreamClass desc = readClassDescriptor();
        String className = desc.getName();

        currentPath.push(className + "@" + handle);
        currentDepth++;

        try {
            // 打印深度信息
            if (currentDepth > maxDiagnosticDepth) {
                throw new DepthLimitExceededException("深度超过阈值: " + maxDiagnosticDepth +
                        "\n当前路径: " + getCurrentPath());
            }

            if (currentDepth % 100 == 0 || objectCount % 1000 == 0) {
                printDiagnosticInfo("处理中", startTime);
            }
            // 实际读取对象
            Object obj = super.readObject();
            // 记录对象引用
            objectHandles.put(handle, getCurrentPath());
            return obj;
        } finally {
            // 清理路径
            currentPath.pop();
            currentDepth--;

            // 打印完成信息
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 50 || currentDepth == 0) {
                printDiagnosticInfo("完成读取", startTime);
            }
        }
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
        // 解析对象时打印信息
        int handle = System.identityHashCode(obj);
        if (objectHandles.containsKey(handle)) {
            printDiagnosticInfo("解析引用: " + objectHandles.get(handle), System.currentTimeMillis());
        }
        return super.resolveObject(obj);
    }

    private void printDiagnosticInfo(String action, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        System.out.printf("[诊断] %s | 深度: %d | 对象数: %d | 路径: %s | 耗时: %dms%n",
                action,
                currentDepth,
                objectCount,
                getCurrentPath(),
                duration);
    }

    private String getCurrentPath() {
        return String.join(" -> ", currentPath);
    }

    private static class DepthLimitExceededException extends RuntimeException {
        public DepthLimitExceededException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            // 创建一个深层次嵌套对象
            oos.writeObject(createDeepObject(500));

            byte[] data = baos.toByteArray();

            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DiagnosticObjectInputStream dis =
                         new DiagnosticObjectInputStream(bais, 300)) {

                System.out.println("开始反序列化诊断...");
                Object obj = dis.readObject();
                System.out.println("反序列化成功完成");
            }
        } catch (DepthLimitExceededException e) {
            System.err.println("深度限制异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 创建深层次嵌套对象
    private static Object createDeepObject(int depth) {
        if (depth <= 0) {
            return "叶子节点";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("level", depth);
        map.put("child", createDeepObject(depth - 1));
        map.put("sibling", createDeepObject(depth - 2));
        return map;
    }
}
