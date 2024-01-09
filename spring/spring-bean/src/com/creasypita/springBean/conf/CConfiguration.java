package com.creasypita.springBean.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by lujq on 1/9/2024.
 */
@Configuration
public class CConfiguration {

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
