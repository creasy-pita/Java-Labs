package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 */
public class WhoCalled {

    static void f() {
        try {
            throw new Exception();
        } catch (Exception e) {
            //会记录整个调用栈，先进后出，比如 main->h->g-f,就会打印出f-g-h-main
            for (StackTraceElement ste : e.getStackTrace()){
                System.out.println(ste.getMethodName());
            }
        }
    }

    static void g(){
        f();
    }

    static void h(){
        g();
    }

    public static void main(String[] args) {
        f();
        System.out.println("---------------------------");
        g();
        System.out.println("---------------------------");
        h();
        System.out.println("---------------------------");

    }
}
