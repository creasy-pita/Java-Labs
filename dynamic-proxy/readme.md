# jdk动态代理实现机制

**接口**

```java
public interface Hello {
    void morning(String name);
}
```

**实现类**

```java
public class HelloWorld implements Hello {
    public void morning(String name) {
        System.out.println("Good morning, " + name);
    }
}
```

**定义用于封装代理逻辑的类，需要实现InvocationHandler接口**

```java
public class HelloInvocationHandler implements  InvocationHandler {

    private Object target;

    public HelloInvocationHandler(Object target){
        this.target = target;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println(System.currentTimeMillis() + " - " + method.getName() + " method start");
        // 调用目标方法
        Object retVal = method.invoke(target, args);
        System.out.println(System.currentTimeMillis() + " - " + method.getName() + " method over");

        return retVal;
    }
}
```

**代理创建类**

JDK 的动态代理主要是通过 JDK 提供的代理创建类 Proxy 为目标对象创建代理，Proxy 中创建代理的方法声明。如下

```java
public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
```

- loader - 类加载器
- interfaces - 目标类所实现的接口列表
- h - 用于封装代理逻辑
- 要求目标类必须实现了接口


**整合测试**

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
public class Main {
    public static void main(String[] args) {
        InvocationHandler handler = new HelloInvocationHandler(new HelloWorld()) ;
        Hello hello = (Hello) Proxy.newProxyInstance(
            Hello.class.getClassLoader(), // 传入ClassLoader
            new Class[] { Hello.class }, // 传入要实现的接口
            handler); // 传入处理调用方法的InvocationHandler
        hello.morning("Bob");
    }
}
```

**jdk创建的代理对象源码**

```java
public class HelloDynamicProxy implements Hello {
    InvocationHandler handler;
    public HelloDynamicProxy(InvocationHandler handler) {
        this.handler = handler;
    }
    public void morning(String name) {
        handler.invoke(
           this,
           Hello.class.getMethod("morning", String.class),
           new Object[] { name });
    }
}
```