package com.creasypita.springBean.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created by lujq on 1/9/2024.
 */
@RestController
public class DemoController {

    @Autowired
    RestTemplate restTemplatea;

//    @Autowired
//    RestTemplate restTemplateb;

    @Autowired
    RestTemplate restTemplateC;
//
//    @Autowired
//    RestTemplate restTemplate;

//    @Resource(name="restTemplatea")
//    RestTemplate restTemplate;

    @Resource(name = "restTemplatea")
    RestTemplate restTemplate1;


    @RequestMapping(value = "/template/homeData",method = {RequestMethod.GET,RequestMethod.DELETE})
    public String getHomeData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        return restTemplatea.exchange("http://192.168.9.83:19010/rulesengine/home/homeData?xmxxBsm=81", HttpMethod.GET, entity, String.class).getBody();
    }
}
