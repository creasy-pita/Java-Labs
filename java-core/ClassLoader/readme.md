

### 场景 自定义classloader加载不同包下的两个同名类

使用两个MyWebAppLoader实例加载不同路径下的com.creasypita.Person的类，调用doSomething方法查看效果，发现是连个隔离的Person类

注意 自定义classloader一般会采用双亲委托先向上完成的查找和加载， 如果还没有找到再从classpath下加载;还没有则从当前自定义classloader的findclass方法来加载。


```java
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

### 


classloader invoke static method

invoke class.forname use classloader
https://www.geeksforgeeks.org/class-fornamestring-boolean-classloader-method-in-java-with-examples/
