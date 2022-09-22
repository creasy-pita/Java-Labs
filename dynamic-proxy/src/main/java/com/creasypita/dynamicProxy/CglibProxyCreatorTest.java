package com.creasypita.dynamicProxy;

/**
 * Created by lujq on 9/22/2022.
 */
public class CglibProxyCreatorTest {

    public static void main(String[] args) {
        try {
            getProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getProxy() throws Exception {
        ProxyCreator proxyCreator = new CglibProxyCreator(new Tank59(), new TankRemanufacture());
        Tank59 tank59 = (Tank59) proxyCreator.getProxy();

        System.out.println("proxy class = " + tank59.getClass() + "\n");
        tank59.run();
        System.out.println();
        System.out.print("射击测试：");
        tank59.shoot();
    }
}
