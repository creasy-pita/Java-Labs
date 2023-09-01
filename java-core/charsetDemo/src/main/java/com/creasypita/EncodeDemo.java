package com.creasypita;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by lujq on 8/31/2023.
 */
public class EncodeDemo {


    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "你好";
        byte[] b1 = s.getBytes("gbk");
        System.out.println(Arrays.toString(b1));
        String s1 = new String(b1, "gbk");
        System.out.println("s1=" + s1);
        String s2 = new String(b1, "ISO8859-1");
        System.out.println("s2=" + s2);

        byte[] b2 = s2.getBytes("gbk");
        System.out.println(Arrays.toString(b2));
        System.out.println(new String(b2, "gbk"));
    }

}

class EncodeDemo2 {

    /**
     * <br>补救还原乱码
     * <br>演示思路
     * <br>gbk的字符串s，在gbk编码后
     * <br>使用ISO8859-1解码得到乱码的字符串s1，但是s1实例内部的字节数据并没有改变，
     * <br>还是原始的字节(使用 bytes[] b1  = s1.getBytes("ISO8859-1")可以获取原始的字节)
     * <br>通过 String s2 = new String(b1, "gbk") 获取到原始正确的字符。
     *
     * <font color="red">注意 如果是用utf-8去解码就没办法还原</font>
     *
     * <br>场景
     * <br> 你好的gbk编码的字节为[-60, -29, -70, -61]
     * <br>客户端url是gbk编码
     * <br>服务端 tomcat默认编码位"ISO8859-1"时，从流中获取的url字符串s1[你好] 会变成乱码 ÄãºÃ 或 ???
     * <br>因为"ISO8859-1"编码虽然获取不到对应的正确字符[你好],但是s1背后的字节没有变
     * <br>所以对s1重新使用"ISO8859-1"获取字节后，可以使用gbk编码获取会原来的数据[你好]
     * @param args
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "你好";
        byte[] b1 = s.getBytes("GBK");
        System.out.println(Arrays.toString(b1));
        //b1是gbk编码的字节，使用"ISO8859-1"解码后是 ÄãºÃ 或 ??? (不同jdk会出现前面两种的一种情况)
        String s1 = new String(b1, "ISO8859-1");
        System.out.println("s1=" + s1);
        //但是知识字符错误，s1背后的字节数据并没有变，还是能得到 [-60, -29, -70, -61]
        byte[] b2 = s1.getBytes("ISO8859-1");
        System.out.println(Arrays.toString(b2));
        String s2 = new String(b2, "gbk");
        System.out.println("s2=" + s2);

    }

}
