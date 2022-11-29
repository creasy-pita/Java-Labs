package com.creasypita.methodParameters;

/**
 * Created by lujq on 11/29/2022.
 */
public class ParameterTransfer {

    public static void main(String[] args) {
        int a = 10;
        // 方法变量如果是基本数据类型，那么采用值传递，也就是方法内部会复制数据到新的内存空间，内部操作的是新的数据
        changeBasicDataTypeValues(a);
        System.out.println(String.format("origin a :%d", a));
    }

    public static void changeBasicDataTypeValues(int a){
        a = a-1;
        System.out.println(String.format("new a:%d", a));
    }
}
