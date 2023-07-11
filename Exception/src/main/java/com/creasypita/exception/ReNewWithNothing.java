package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 * 重新创建异常，并忽略内层的异常，这样内部的异常会被隐藏，外界没有感知
 */
public class ReNewWithNothing {
    public static void f(){
        throw new NullPointerException("NullPointerException f");
    }

    public static void g() throws Exception {
        try {
            f();
        } catch (NullPointerException e) {
            throw new Exception();
        }
    }

    public static void main(String[] args) {
        try {
            g();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
