# Spring 事务传播行为详解

> :bulb: 围绕示例进行调制来详解

## 1. 什么是事务传播行为？



事务传播行为用来描述由某一个事务传播行为修饰的方法被嵌套进另一个方法的时事务如何传播。

用伪代码说明：

```java
 public void methodA(){
    methodB();
    //doSomething
 }
 
 @Transaction(Propagation=XXX)
 public void methodB(){
    //doSomething
 }
```

代码中`methodA()`方法嵌套调用了`methodB()`方法，`methodB()`的事务传播行为由`@Transaction(Propagation=XXX)`设置决定。这里需要注意的是`methodA()`并没有开启事务，某一个事务传播行为修饰的方法并不是必须要在开启事务的外围方法中调用。

## 2. Spring中七种事务传播行为



| 事务传播行为类型                                             | 说明 |
| :----------------------------------------------------------- | ---- |
| PROPAGATION_REQUIRED                 |  如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中。这是最常见的选择。    |
| PROPAGATION_SUPPORTS                |  支持当前事务，如果当前没有事务，就以非事务方式执行。     |
| PROPAGATION_SUPPORTS                 | 支持当前事务，如果当前没有事务，就以非事务方式执行。     |
| PROPAGATION_MANDATORY                 |使用当前的事务，如果当前没有事务，就抛出异常。      |
| PROPAGATION_REQUIRES_NEW                 | 新建事务，如果当前存在事务，把当前事务挂起。     |
| PROPAGATION_NOT_SUPPORTED                 | 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。     |
| PROPAGATION_NEVER                 |以非事务方式执行，如果当前存在事务，则抛出异常。      |
| PROPAGATION_NESTED                 | 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION_REQUIRED类似的操作。     |

​                                                                                                                                                                                                        

## 3. 代码验证

文中代码以传统三层结构中两层呈现，即Service和Dao层，由Spring负责依赖注入和注解式事务管理，DAO层由Mybatis实现，你也可以使用任何喜欢的方式，例如，Hibernate,JPA,JDBCTemplate等。数据库使用的是MySQL数据库，你也可以使用任何支持事务的数据库，并不会影响验证结果。

首先再数据库层创建2张表

```sql
CREATE TABLE `student1` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL DEFAULT '',
    PRIMARY KEY(`id`)
) ENGINE = InnoDB;
CREATE TABLE `student2` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL DEFAULT '',
    PRIMARY KEY(`id`)
) ENGINE = InnoDB;
```

编写Bean和Dao层

Student1:

```java
public class Student1 {
    private int id;
    private String name;

    public Student1() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

Student2:

```
public class Student2 {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
```

Student1Mapper,Student2Mapper

```java
@Component
public interface Student1Mapper {
    Student1 getById(int id);
    void insertStudent(Student1 student);

}
@Component
public interface Student2Mapper {
    Student1 getById(int id);
    void insertStudent(Student1 student);

}
```



#### 1.PROPAGATION_REQUIRED

我们为`Student1Service`和`Student2Service`相应方法加上`Propagation.REQUIRED`属性

```java
@Service
public class Student1Service {
    @Autowired
    private Student1Mapper student1Mapper;


    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequired(Student1 student){
        student1Mapper.insertStudent(student);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredException(Student1 student){
        student1Mapper.insertStudent(student);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredWithSQLExceptionCatched(Student1 student){
        try {
            //手动制造sql异常
            student.setName("55555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555");
            student1Mapper.insertStudent(student);
        }catch (Exception ex){
            System.out.println("Student1Service 发生异常");
        }
    }
}
```



```java
@Service
public class Student2Service {

    @Autowired
    private Student2Mapper student2Mapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequired(Student2 student){
        student2Mapper.insertStudent(student);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredException(Student2 student){
        student2Mapper.insertStudent(student);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredWithInnerSQLExceptionCatched(Student2 student){
        try {
            //手动制造sql异常
            student.setName("55555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555");
            student2Mapper.insertStudent(student);
        }catch (Exception ex){
            System.out.println("Student2Service 发生异常");
        }
    }
}
```



##### 场景1：外围方法没有开启事务

验证方法1：

参考： notransaction_exception_required_required | notransaction_required_required_exception



**结论：通过这两个方法我们证明了在外围方法未开启事务的情况下`Propagation.REQUIRED`修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。**



##### 场景2 ： 外围方法开启事务，这个是使用率比较高的场景。

验证方法1：

```java
@Transactional(propagation = Propagation.REQUIRED)
public void transaction_exception_required_required(){
    Student1 student1 = new Student1();
    student1.setName("a");
    Student2 student2 = new Student2();
    student2.setName("b");

    service1.insertRequired(student1);
    service2.insertRequired(student2);
    throw new RuntimeException();
}
```

验证方法2：

```java
    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_exception(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredException(student2);
    }
```

验证方法3：

```java
    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_exception_try(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");
        service1.insertRequired(student1);
        try {
            service2.insertRequiredException(student2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```



验证方法4：

```java
    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_InnerSQLExceptionCatched(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredWithInnerSQLExceptionCatched(student2);
    }
```



| 方法序号 | 执行结果       | 备注                                                         |
| -------- | -------------- | ------------------------------------------------------------ |
| 1        | a,b均未插入    | 外部方法向上抛出异常，外部方法回滚，内部方法也要回滚         |
| 2        | a,b均未插入    | 内部方法向上抛出异常，外部方法回滚，其他的内部方法都会回滚   |
| 3        | a,b均未插入    | 内部方法向上抛出异常，内部方法回滚，外部方法即使捕获异常，试图提交也会报错，同时整个会回滚 |
| 4        | a插入，b未插入 | 内部方法抛出sql异常内部捕获，外围方法正常提交，内部抛出sql异常的部分的b没有插入 |



##### 结论

以上验证1-3告诉我们，外围方法开启事务的情况下，`Propagation.REQUIRED`修饰的内部方法会加入到外围方法的事务中，所有 `Propagation.REQUIRED`修改的外部和内部方法在一个事务中，只要一个抛出异常，就都会回滚。

验证方法4告诉我们，方法内部的sql异常内部捕获，外部事务类是没有感知的，错误会被忽略，不会直接回滚。



##### 额外说明

①`CommonService:transaction_required_required_exception_try`方法与 ②`transaction_required_required_InnerSQLExceptionCatched`方法结果不同的原因分析：

①中是`Student2Service：insertRequiredException`方法内部发生异常被事务切面catch时，**会记录事务状态需要回滚**，但是后续代码的逻辑是外围的①方法catch异常，所以会试图提交，提交时发现事务时需要回滚的状态，报错： **Transaction rolled back because it has been marked as rollback-only**。

②中`Student2Service：insertRequiredWithInnerSQLExceptionCatched`方法内部发生SQLException异常，又catch了异常，所以外围不会感知，后续还是会提交。所以结果是：a插入成功，b没有插入。

这里的一个疑惑时，内部发生SQLException异常,数据库端应该时直接主动执行回滚才对。这里其实不是的，因为回滚和提交是交给开发人员自己控制的。比如：

```sql
BEGIN ;

insert into student (`name` )values('ddddd');
-- name 过长，会执行失败：  1406 - Data too long for column 'NAME' at row 1
insert into student (`name`) values('11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111');

COMMIT;
```

执行结果是:第一句执行成功，第二句执行报错，第三句 `COMMIT`并没有执行，等待开发人员决定提交或者回滚。 

```log
--这一句执行成功
insert into student (`name` )values('ddddd')
> Affected rows: 1
> Time: 0s
-- name 过长，会执行失败
insert into student (`name`) values('11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111')
> 1406 - Data too long for column 'NAME' at row 1
> Time: 0s
```



## 代码： 
