package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 * 重新创建异常，通过initCause保留内层的异常栈
 */
public class ReNewWithInitCause {

    public static void f(){
        throw new NullPointerException("NullPointerException f");
    }

    public static void g() throws Exception {
        try {
            f();
        } catch (NullPointerException e) {
            Exception ex = new Exception();
            ex.initCause(e);
            throw ex;
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
