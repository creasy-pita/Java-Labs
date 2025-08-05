package com.creasypita.obj;

import java.io.Serializable;

/**
 * Created by lujq on 8/3/2025.
 */
public class User implements Serializable {
    private String name;

    private Department department;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
