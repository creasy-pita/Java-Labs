package com.creasypita.methodParameters;

/**
 * Created by lujq on 6/25/2023.
 */
public class TestFun2 {
    public static void testStr(String str){
        str="hello";//型参指向字符串 “hello”
    }
    public static void main(String[] args) {
        String s="1" ;
        TestFun2.testStr(s);
        System.out.println("s="+s); //实参s引用没变，值也不变
    }
}
