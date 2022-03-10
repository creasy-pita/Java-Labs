package com.creasypita.Jprofile;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by lujq on 3/10/2022.
 */
public class GCRootsTest {
    public static void main(String[] args) {
        Date now = new Date();
        ArrayList<Object> objects = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            objects.add(String.valueOf(i));
        }
        System.out.println("数据已经准备，请操作");
        new Scanner(System.in).next();
        now = null;
        objects = null;
        System.out.println("数据已经置空，请操作");
        new Scanner(System.in).next();
        System.out.println("结束");
    }


}
