package com.creasypita.springBean.conf;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by lujq on 1/9/2024.
 */
@Configuration
public class BConfiguration {

    @Configuration
    public static class InnerBConf{

        //通过参数名restTemplateb来确定 beanname名称为restTemplateb的才会注入。  如果spring容器中不存在restTemplateb，会按类型查找获取RestTemplate实例
        @Bean
        public String aString(RestTemplate restTemplateb) {
            RestTemplate b = restTemplateb;
            System.out.println("restTemplate in aString:" + b.hashCode());
            return "";
        }

        @Bean
        public RestTemplate restTemplateE() {
            RestTemplate a = new RestTemplate();
            System.out.println("restTemplateE:" + a.hashCode());
            return a;
        }
    }

}
