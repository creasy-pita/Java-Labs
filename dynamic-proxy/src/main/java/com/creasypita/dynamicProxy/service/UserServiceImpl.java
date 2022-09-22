package com.creasypita.dynamicProxy.service;

/**
 * Created by lujq on 9/22/2022.
 */
public class UserServiceImpl implements UserService {


    public void addUser(Object user) {
        System.out.println("save user info");
    }


    public void update(Object user) {
        System.out.println("update user info");
    }
}
