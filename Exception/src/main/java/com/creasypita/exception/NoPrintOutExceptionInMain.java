package com.creasypita.exception;

/**
 * Created by lujq on 8/15/2023.
 * main方法接收到异常，不直接打印
 */
public class NoPrintOutExceptionInMain {

    public static void main(String[] args) {
        g();
    }

    public static void g(){
        try {
            f();
        } catch (Exception e) {
            //空参打印会带红底色，System.out流则没有
            e.printStackTrace(System.out);
            throw e;
        }
    }

    public static void f() {
        throw new RuntimeException("f");
    }
}
