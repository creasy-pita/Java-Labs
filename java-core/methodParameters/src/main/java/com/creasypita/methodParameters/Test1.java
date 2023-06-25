package com.creasypita.methodParameters;

/**
 * Created by lujq on 6/25/2023.
 */
public class Test1 {
    public static void main(String[] args) {
        int a = 100;
        System.out.println("形参传值调用：实参a 初始值" + a);
        test(a);
        System.out.println("形参传值调用：实参a 修改后的值" + a);
    }

    public static void test(int a){
        a = a +1;
        System.out.println("形参传值调用：形参a 修改值" + a);
    }
}
