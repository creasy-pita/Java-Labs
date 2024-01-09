
spring ioc注解@AutoWired，@Resourcedemo
@AutoWired 先按类型查找，没有则抛异常；有则再按参数名称作为beanname查找,有一个则放回；没有则按类型查找，判断是否有多个，有多个则抛异常；
@Resource  先按注解中的name值作为beanname查找，查找不到抛异常；如果没有指定name，按变量名称作为beanname查找，查找不到则抛异常；