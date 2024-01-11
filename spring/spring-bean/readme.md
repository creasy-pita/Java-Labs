
## spring ioc注解@AutoWired，@Resourcedemo

@AutoWired 先按类型查找，没有则抛异常；有则再按参数名称作为beanname查找,有一个则放回；没有则按类型查找，判断是否有多个，有多个则抛异常；
@Resource  先按注解中的name值作为beanname查找，查找不到抛异常；如果没有指定name，按变量名称作为beanname查找，查找不到则抛异常；



## java Configuration 注解类CConfiguration中`@Resource`注解成员的解析

`@Resource`成员restTemplate 因为在构造CConfiguration实例时就注入，此时restTemplateC，restTemplateD还没有注入，所以bb方法中的`restTemplate`不会是`restTemplateC`，`restTemplateD`

```java
@Configuration
public class CConfiguration {

    @Resource
    RestTemplate restTemplate;

    //此处的restTemplate 因为在构造CConfiguration实例时就注入，此时restTemplateC，restTemplateD还没有注入
    @Bean
    public String bb(){
        System.out.println( "bb's restTemplate hashcode :" + this.restTemplate.hashCode());
        return "";
    }

    @Bean
    public RestTemplate restTemplateC() {
        RestTemplate a = new RestTemplate();
        System.out.println("restTemplateC:" + a.hashCode());
        return a;
    }

    @Bean
    public RestTemplate restTemplateD() {
        RestTemplate b = new RestTemplate();
        System.out.println("restTemplateD:" + b.hashCode());
        return b;
    }

}
```