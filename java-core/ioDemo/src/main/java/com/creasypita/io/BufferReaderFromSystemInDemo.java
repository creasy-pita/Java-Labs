package com.creasypita.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by lujq on 9/5/2023.
 */
public class BufferReaderFromSystemInDemo {

    public static void main(String[] args) {
        //从控制台输入，输出over或者ctrl+c时结束

        BufferedReader bfr = null;
        String s = null;
        try{
            bfr = new BufferedReader(new InputStreamReader(System.in));
            while ((s = bfr.readLine()) != null) {
                if ("over".equals(s)) {
                    break;
                }
                System.out.println(s.toUpperCase(Locale.ROOT));
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            /**
             * 流的关闭放到finally，保证一定能执行到
             * 关闭时先判空
             * 流的关闭可能出现异常，需要处理异常
             */
            try {
                if (bfr !=null) {
                    bfr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
