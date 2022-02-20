package com.creasypita.resttemplate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by lujq on 11/23/2021.
 */
@RestController
public class DemoController {
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/template/homeData",method = {RequestMethod.GET,RequestMethod.DELETE})
    public String getHomeData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);

        return restTemplate.exchange("http://192.168.9.83:19010/rulesengine/home/homeData?xmxxBsm=81", HttpMethod.GET, entity, String.class).getBody();
    }
    @RequestMapping(value = "/template/a")
    public String restTemplateWithHttpStatusCodeException(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:10000/template/b", HttpMethod.GET, entity, String.class);
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            System.out.println("跑错了");
            System.out.println( e.getStatusCode());
            String.valueOf(e.getResponseBodyAsByteArray());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }

        return  responseEntity.getBody();
    }

    @RequestMapping(value = "/template/c")
    public String restTemplate_status500_in_responseentityMap(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:10000/template/b", HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }

        return  responseEntity.getBody();
    }

    @RequestMapping(value = "/template/b")
    public String getB() throws Exception {
        ResponseEntity<String> responseEntity = new ResponseEntity<String>("500",HttpStatus.INTERNAL_SERVER_ERROR);
        throw new Exception("jjjjjjj");
    }

    @RequestMapping(value = "/template/d")
    public String getD() throws Exception {
        return "ddddd";
    }

}
