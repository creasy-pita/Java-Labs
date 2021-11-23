package com.creasypita.resttemplate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by lujq on 11/23/2021.
 */
@RestController
public class DemoController {
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/template/homeData")
    public String getHomeData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);

        return restTemplate.exchange("http://192.168.9.83:19010/rulesengine/home/homeData?xmxxBsm=81", HttpMethod.GET, entity, String.class).getBody();
    }
}
