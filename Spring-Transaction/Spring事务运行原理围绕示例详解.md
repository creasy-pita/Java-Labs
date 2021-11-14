# Spring 事务运行原理围绕示例详解

场景：事务运行原理围绕`PROPAGATION_REQUIRED` 中 `CommonService:transaction_required_required_exception_try`方法详解

执行步骤如下：

- 1 CommonService:transaction_required_required_exception_try方法进入前，会 `TransactionAspectSupport`层会开启事务

- 2 方法中`service1.insertRequired(student1)`正常执行

- 3 `service2.insertRequiredException(student2)`内部抛异常， `TransactionAspectSupport`会接受到异常，会走捕获异常的逻辑，

  - 3.1 如果是新事务，则直接操作回滚。
  - 3.2 如果是传播过来的事务，则会给事务状态记录需回滚的状态。
  - 3.3 重新抛出异常（会抛到外围的 `CommonService:transaction_required_required_exception_try`方法）

- 4  `CommonService:transaction_required_required_exception_try`方法因为捕获了异常，整个结束后走到 `TransactionAspectSupport` 中会做提交的方法,方法内部会发现事务实际处于需回滚状态，所以会报错： **Transaction rolled back because it has been marked as rollback-only**。



```java
//TransactionInterceptor.java
public Object invoke(MethodInvocation invocation) throws Throwable {
    // Work out the target class: may be {@code null}.
    // The TransactionAttributeSource should be passed the target class
    // as well as the method, which may be from an interface.
    Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

    // Adapt to TransactionAspectSupport's invokeWithinTransaction...
    return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
}

// TransactionAspectSupport.java  事务增强类  处理事务的关键类；
protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
                                         final InvocationCallback invocation) throws Throwable {

    // If the transaction attribute is null, the method is non-transactional.
    TransactionAttributeSource tas = getTransactionAttributeSource();
    final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
    final PlatformTransactionManager tm = determineTransactionManager(txAttr);
    final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

    if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
        //声明式事务
        // 根据目标方法识别名，事务管理器，配置的事务属性获取事务信息
        // Standard transaction demarcation with getTransaction and commit/rollback calls.
        TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);

        Object retVal;
        try {
            // This is an around advice: Invoke the next interceptor in the chain.
            // This will normally result in a target object being invoked.
            retVal = invocation.proceedWithInvocation();
        }
        catch (Throwable ex) {
            // <2.1> 方法出错 则处理回滚
            // target invocation exception
            completeTransactionAfterThrowing(txInfo, ex);
            // <2.2> 向上抛异常
            throw ex;
        }
        finally {
            cleanupTransactionInfo(txInfo);
        }
        //<3.1>执行到这里 说明可以操作提交了
        commitTransactionAfterReturning(txInfo);
        return retVal;
    }

    else {
        //编程式事务 省略...
    }
}
```





```java
// CommonService.java   
@Transactional
    public void transaction_required_required_exception_try(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");
        service1.insertRequired(student1);
        try {
            service2.insertRequiredException(student2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

