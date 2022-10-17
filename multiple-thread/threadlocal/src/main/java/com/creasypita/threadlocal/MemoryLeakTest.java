package com.creasypita.threadlocal;

import com.github.pagehelper.PageHelper;

/**
 * Created by lujq on 11/8/2021.
 */
public class MemoryLeakTest {
    //    会提示堆内存溢出
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
//                    TestClass t = new TestClass(i);
//                    t.printId();
//                    t = null;
                }
            }
        }).start();
    }
    // 解决了泄漏
//    public static void main(String[] args) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    TestClass t = new TestClass(i);
//                    t.printId();
//                    t.threadLocal.remove();
//                }
//            }
//        }).start();
//    }


//    static class TestClass{
//        private int id;
//        private int[] arr;
//        private ThreadLocal<TestClass> threadLocal;
//        TestClass(int id){
//            this.id = id;
//            arr = new int[10000000];
//            threadLocal = new ThreadLocal<>();
//            threadLocal.set(this);
//        }
//
//        public void printId(){
//            System.out.println(threadLocal.get().id);
//        }
//    }
}

