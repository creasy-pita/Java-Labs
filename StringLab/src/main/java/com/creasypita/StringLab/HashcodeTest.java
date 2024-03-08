package com.creasypita.StringLab;

/**
 * Created by lujq on 3/8/2024.
 */
public class HashcodeTest {

    public static void main(String[] args) {
        String a = "通话";
        String b = "重地";
        System.out.println(a.hashCode() + "：" + b.hashCode());
        System.out.println(a.equals(b));
    }
}
