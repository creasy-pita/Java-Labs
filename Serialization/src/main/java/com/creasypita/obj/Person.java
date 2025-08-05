package com.creasypita.obj;

import java.io.Serializable;
/**
 * Created by lujq on 8/5/2025.
 */
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int age;
    private transient String address; // transient字段不会被序列化

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    // getters, setters 和 toString() 省略

    @Override
    public String toString(){
        return "{name: " + this.name
                + " age: " + this.age
                + " address: " + this.address + "}";
    }
}
