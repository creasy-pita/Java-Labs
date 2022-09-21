package com.creasypita.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lujq on 9/7/2022.
 */
public class Test {
    public static void main(String[] args) {
        Pair<String> stringPair = new Pair<>();
//        stringPair.setFirst("1");
//        stringPair.setFirst("2");
//        System.out.println(stringPair.getList("3"));

//        ArrayList<String> strings = new ArrayList<>();
//        strings.add("1");
//        strings.add("2");
//        System.out.println(stringPair.getList1(strings));

        ArrayList<Integer> integers = new ArrayList<>();

        integers.add(new Integer("1"));
        integers.add(new Integer("2"));
        List s = stringPair.getList1(integers);
        System.out.println(stringPair.getList1(integers));
    }
}
