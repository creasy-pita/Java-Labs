package com.creasypita.obj;

import java.io.Serializable;

/**
 * Created by lujq on 8/3/2025.
 */
public class Department implements Serializable {
    private String name;
    private Leader leader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }
}
