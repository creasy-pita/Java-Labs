package com.creasypita.abstractDemo;

/**
 * Created by lujq on 1/11/2022.
 */
public abstract class Person {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getDescription();

    public abstract String getAge();
}
