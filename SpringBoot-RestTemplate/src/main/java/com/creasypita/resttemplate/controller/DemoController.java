package com.creasypita.resttemplate.controller;

import com.creasypita.resttemplate.utils.ResponseEntityWrapper;
import com.creasypita.resttemplate.utils.RestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lujq on 11/23/2021.
 */
@RestController
public class DemoController {
    @Autowired
    RestTemplate restTemplate;

    //[异常断点]todo 用于测试异常断点，请勿修改balala
    @RequestMapping(value = "/template/a")
    public String restTemplateWithHttpStatusCodeException(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity <String> entity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = null;
//[异常断点]注释try catch，在增加异常断点，类型为 HttpServerErrorException，就可以进行调试
//[异常断点]注释try catch，在增加异常断点，类型为 Exception，就可以进行调试并查看到过程中的所有断点
//        如果suspend 选中all,就可以查看阻塞并定位到所有线程抛出的Exception
//        如果suspend 选中thread,就可以查看阻塞并定位到当前线程抛出的Exception
//        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:10000/template/b", HttpMethod.GET, entity, String.class);
//        } catch (HttpServerErrorException | HttpClientErrorException e) {
//            System.out.println("跑错了");
//            System.out.println( e.getStatusCode());
//            String.valueOf(e.getResponseBodyAsByteArray());
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.out.println("跑错了");
//            e.printStackTrace();
//        }

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

    //普通get请求  + 使用 content-type/text-plain + 传formdata
    @RequestMapping(value = "/template/get_textplain_fromData")
    public String get_textplain_fromData(){
        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithget_fromdata?a=aaa", HttpMethod.GET,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    //普通get请求  + 使用 content-type/application/x-www-form-urlencoded + 传form-data数据
    //因为get请求不会提交表单数据，所以表单数据是空的
    @RequestMapping(value = "/template/get_application_form_urlencoded_fromData")
    public String get_application_form_urlencoded_fromData(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("1","111");
        map.add("2","222");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithget_fromdata?a=aaa", HttpMethod.GET,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    //普通post请求  + 使用 content-type/application/x-www-form-urlencoded + 传form-data数据
    @RequestMapping(value = "/template/post_application_form_urlencoded_fromdatawithmultivaluemap")
    public String post_application_form_urlencoded_fromDataWithMultiValueMap(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("1","111");
        map.add("2","222");
        map.put("3", Arrays.asList("3", "33"));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_multivaluemap?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    //普通post请求  + 使用 content-type/application/x-www-form-urlencoded + 传form-data数据
    @RequestMapping(value = "/template/post_application_form_urlencoded_fromdata_map")
    public String post_application_form_urlencoded_fromdata_map(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> map = new HashMap<>();
        map.put("1","111");
        map.put("2","222");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_map?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    @RequestMapping(value = "/template/post_application_form_urlencoded_fromdata_object")
    public String post_application_form_urlencoded_fromdata_object(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("1","111");
        map.add("2","222");
        map.put("3", Arrays.asList("3", "33"));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_object?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }


    //post请求 + 使用content-type/json + body中传json数据
    @RequestMapping(value = "/template/post_application_json_object")
    public String post_application_json_object(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("1","111");
        map.put("2","222");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_object?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    //post请求 + 使用content-type/json + body中传json数据
    @RequestMapping(value = "/template/post_application_json_map")
    public String post_application_json_map(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("1","111");
        map.add("2","222");
        map.put("3", Arrays.asList("3", "33"));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers );
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_map?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

    @RequestMapping(value = "/templateutil/post_application_json")
    public String post_application_json(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = "{\n" +
                "    \"path\": \"\",\n" +
                "    \"subGuid\": \"2bd388a1-c550-456c-9e6a-58530a92db43\",\n" +
                "    \"exportList\": [\n" +
                "        \"role\",\n" +
                "        \"loginpage\",\n" +
                "        \"bizmodel\",\n" +
                "        \"homepage\",\n" +
                "        \"keyvalue\",\n" +
                "        \"func\",\n" +
                "        \"subsystem\",\n" +
                "        \"rule\"\n" +
                "    ],\n" +
                "    \"expBIForm\": false,\n" +
                "    \"expShare\": false,\n" +
                "    \"expRule\": true,\n" +
                "    \"dataSourceType\": \"mysql\"\n" +
                "}";
        HttpEntity<String> entity = new HttpEntity<>(json);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange("http://192.168.100.66:2900/test/typewithpost_fromdata_map?a=aaa", HttpMethod.POST,entity, String.class);
        } catch (Exception e) {
            System.out.println("跑错了");
            e.printStackTrace();
        }
        return  responseEntity.getBody();
    }

}
