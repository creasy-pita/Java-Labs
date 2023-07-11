package com.creasypita.exception;

/**
 * Created by lujq on 7/11/2023.
 * 使用fillInStackTrace覆盖内部的异常抛出点，使用当前方法作为新的异常抛出点
 */
public class ReThrowWithFillInStackTrace {
    public static void f()throws Exception{
        throw new Exception("Exception: f()");
    }

    public static void g() throws Exception{
        try{
            f();
        }catch(Exception e){
            System.out.println("inside g()");
            throw (Exception) e.fillInStackTrace();
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
