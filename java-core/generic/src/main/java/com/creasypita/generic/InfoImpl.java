package com.creasypita.generic;

/**
 * Created by lujq on 9/7/2022.
 */
public class InfoImpl<T> implements Info<T>{   // 定义泛型接口的子类
    private T var ;             // 定义属性
    public InfoImpl(T var){     // 通过构造方法设置属性内容
        this.setVar(var) ;
    }
    public void setVar(T var){
        this.var = var ;
    }
    public T getVar(){
        return this.var ;
    }
}
