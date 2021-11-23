package com.creasypita.StringLab;

import sun.rmi.runtime.Log;

/**
 * Created by lujq on 11/16/2021.
 */
public class StringPoolTest {

    public static void main(String[] args) {
        String str1 = "abc";
        String str2 = "abc";
        String str3 = new String("abc");
        String str4 = new String("abc");
        String str5 = new String("abc").intern();
        /**
         第一种 ： 字符串字面量 string literal   会使用string pool常量池
         第二种：new String 会使用java heap
         实际string使用的char[] 第二种情况是同一个，
         解释：常量池中有则会去使用，没有则会单独开辟空间放在java heap 而不是常量池；
         常量池字符串对象是java程序整个生命周期，所以new String的由于量大经常变化，所以尽可能不用到常量池；
         只有在常量池已经有的情况下会复用
         常量池中可能是一个map,key 是？ value是？
         **/
        System.out.println(str1 == str2);//true
        System.out.println(str1 == str3);//false
        //str3 str4 都属于第二种
        System.out.println(str3 == str4);//false
        //intern在newString 中使用，会使得str5指向string pool

        System.out.println(str1 == str5);//true

    }
}
