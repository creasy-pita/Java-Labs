package com.creasypita.springBean.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Created by lujq on 1/9/2024.
 */
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
