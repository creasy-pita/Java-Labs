package com.creasypita.methodParameters;

/**
 * Created by lujq on 6/25/2023.
 */
public class TestFun4 {
    public static void testStringBuffer(StringBuffer sb){
        sb.append("java");//改变了实参的内容
    }
    public static void main(String[] args) {
        StringBuffer sb= new StringBuffer("my ");
        new TestFun4().testStringBuffer(sb);
        System.out.println("sb="+sb.toString());//内容变化了
    }
}
