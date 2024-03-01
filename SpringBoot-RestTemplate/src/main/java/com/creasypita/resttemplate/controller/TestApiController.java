package com.creasypita.resttemplate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lujq on 11/23/2021.
 * 业务接口
 */
@RestController
public class TestApiController {
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/test/typewithget_fromdata",method = {RequestMethod.GET,RequestMethod.DELETE})
    public String typewithget_fromdata(@RequestParam String a, @RequestBody(required = false) MultiValueMap<String, Object> formBody) {
        System.out.println("param a:" + a);
        System.out.println(formBody);
        return "1";
    }

    @RequestMapping(value = "/test/typewithpost_fromdata_map",method = {RequestMethod.POST})
    public String typewithpost_fromdata_map(@RequestParam String a, @RequestBody(required = false) HashMap<String, Object> formBody) {
        System.out.println("param a:" + a);
        System.out.println(formBody);
        return "1";
    }

    @RequestMapping(value = "/test/typewithpost_fromdata_object",method = {RequestMethod.POST})
    public String typewithpost_fromdata_object(@RequestParam String a, @RequestBody(required = false) Object formBody) {
        System.out.println("param a:" + a);
        System.out.println(formBody);
        return "1";
    }

    @RequestMapping(value = "/test/typewithpost_fromdata_multivaluemap",method = {RequestMethod.POST})
    public String typewithpost_fromdata_multivaluemap(@RequestParam String a, @RequestBody(required = false) MultiValueMap<String, Object> formBody) {
        System.out.println("param a:" + a);
        System.out.println(formBody);
        return "1";
    }

}
