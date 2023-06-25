
Java中的形参和实参的区别以及传值调用和传引用调用

### 参数是基本数据类型时使用值传递（形参和实参不是同一份数据），形参值修改不影响实参的值

```java
public class Test1 {
    public static void main(String[] args) {
        int a = 100;
        System.out.println("形参传值调用：实参a 初始值" + a);
        test(a);
        System.out.println("形参传值调用：实参a 修改后的值" + a);
    }

    public static void test(int a){
        a = a +1;
        System.out.println("形参传值调用：形参a 修改值" + a);
    }
}
```

### 参数是引用类型时使用引用传递，把实参传递给形参，形参的引用改变了，实参的引用不变，值也不变

```java
public class TestFun2 {
    public static void testStr(String str){
        str="hello";//型参指向字符串 “hello”
    }
    public static void main(String[] args) {
        String s="1" ;
        TestFun2.testStr(s);
        System.out.println("s="+s); //实参s引用没变，值也不变
    }
}

```

### 参数是引用类型时使用引用传递，把实参传递给形参，形参的内容改变了，实参的内容也改变了

```java
public class TestFun4 {
    public static void testStringBuffer(StringBuffer sb){
        sb.append("java");//改变了实参的内容
    }
    public static void main(String[] args) {
        StringBuffer sb= new StringBuffer("my ");
        new TestFun4().testStringBuffer(sb);
        System.out.println("sb="+sb.toString());//内容变化了
    }
}
```


