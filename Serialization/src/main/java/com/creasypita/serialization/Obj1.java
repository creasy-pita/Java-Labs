package com.creasypita.serialization;

/**
 * Created by lujq on 11/22/2021.
 */

import java.io.Serializable;

public class Obj1 implements Serializable {
    private static final long serialVersionUID = 1L;

    private String f1;

    private String f2;

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }
}
