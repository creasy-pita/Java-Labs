package com.creasypita;

/**
 * Created by lujq on 5/20/2023.
 * RemoveAbandon测试：使用的连接有租期，租期到了就回收
 */
public class RemoveAbandonTest {
    public static void main(String[] args) {
        new MyThread().start();
        new MyThread().start();
    }
}
