package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 * 重新创建异常，通过传递内存异常对象到构造方法的方式来保留内层异常信息
 */
public class ReNewWithPassInnerExceptionToConstructor {
    public static void f(){
        throw new NullPointerException("inside f");
    }

    public static void g() throws Exception {
        try {
            f();
        } catch (NullPointerException e) {
            throw new Exception(e);
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
