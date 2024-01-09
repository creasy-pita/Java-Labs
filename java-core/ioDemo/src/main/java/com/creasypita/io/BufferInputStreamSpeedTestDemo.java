package com.creasypita.io;

import java.io.*;

/**
 * bufferinputStream 和普通 fileinputstream读取速度pk测试 缓冲流为什么会提高io速度
 */
public class BufferInputStreamSpeedTestDemo {

    private final static String FILENAME = "input-test.txt";

    public static void main(String[] args) {
        testOutputStream();
        //最终调用系统本地方法读取一个字节，所以效率低
        testInputStream(false);
        testInputStream(true);
        //最终调用本地方法批量读取到缓冲区，所以效率高
        testBufferInputStream();

    }

    /**
     *
     * @param isbuffer  是否使用原生的inputstream中的byte[]
     */
    public static void testInputStream(boolean isbuffer) {

        FileInputStream in = null;
        int byteLen = 8192;
        // 每次读取1024字节
        byte[] buffer = new byte[byteLen];

        try {
            in = new FileInputStream(FILENAME);
            long start = System.currentTimeMillis();

            while (true) {
                int read;
                if (isbuffer) {
                    read = in.read(buffer);
                    if (read == -1 || read < byteLen) {
                        break;
                    }
                }else {
                    read = in.read();
                    if (read == -1) {
                        break;
                    }
                }

            }
            long end = System.currentTimeMillis();

            System.out.println("total = " + (end - start));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void testBufferInputStream() {
        FileInputStream inputStream = null;
        BufferedInputStream in = null;
        try {
            inputStream = new FileInputStream(FILENAME);
            in = new BufferedInputStream(inputStream);

            long start = System.currentTimeMillis();
            while (true) {
                int read = in.read();
                if (read == -1) {
                    break;
                }
            }
            long end = System.currentTimeMillis();

            System.out.println("total = " + (end - start));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void testOutputStream() {
        String context = "年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万，年薪百万\r\n";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(FILENAME);
            // 文件大概7.8M
            for (int i = 0; i < 50000; i++) {
                fileWriter.append(context);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
