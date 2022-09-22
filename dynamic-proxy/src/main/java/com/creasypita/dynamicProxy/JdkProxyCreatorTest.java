package com.creasypita.dynamicProxy;

import com.creasypita.dynamicProxy.service.UserService;
import com.creasypita.dynamicProxy.service.UserServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by lujq on 9/22/2022.
 */
public class JdkProxyCreatorTest {

    public static void main(String[] args) {
        try {
            getProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getProxy() throws Exception {
        ProxyCreator proxyCreator = new JdkProxyCreator(new UserServiceImpl());
        UserService userService = (UserService) proxyCreator.getProxy();

        System.out.println("proxy type = " + userService.getClass());
        System.out.println();
        userService.addUser(null);
        System.out.println();
        userService.update(null);
    }

}
