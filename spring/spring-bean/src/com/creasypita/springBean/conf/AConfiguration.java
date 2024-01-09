package com.creasypita.springBean.conf;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by lujq on 1/9/2024.
 */
@Configuration
public class AConfiguration {

    @Bean
    public RestTemplate restTemplatea() {
        RestTemplate a = new RestTemplate();
        System.out.println("restTemplatea:" + a.hashCode());
        return a;
    }

    @Bean
//    @ConditionalOnProperty(prefix = "aaaa", name="bbb", havingValue = "true", matchIfMissing = false)
    public RestTemplate restTemplateb() {
        RestTemplate b = new RestTemplate();
        System.out.println("restTemplateb:" + b.hashCode());
        return b;
    }

}
