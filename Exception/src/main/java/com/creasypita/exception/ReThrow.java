package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 * 外层方法捕获内层异常后重新抛出
 */
public class ReThrow {
    public static void f()throws Exception{
        throw new Exception("Exception: f()");
    }

    public static void g() throws Exception{
        try{
            f();
        }catch(Exception e){
            System.out.println("inside g()");
            throw e;
        }
    }
    public static void main(String[] args){
        try{
            g();
        }
        catch(Exception e){
            System.out.println("inside main()");
            e.printStackTrace(System.out);
        }
    }
}
