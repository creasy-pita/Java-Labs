package com.creasypita.json;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by lujq on 3/19/2024.
 */
public class JsonSerializableTests {

    private static DemoBean bean;

    private static List<Map<String, DemoBean>> list;


    static {
        bean = new DemoBean();
        bean.setId(1000);
        bean.setFresh(Boolean.TRUE);
//        bean.setContext("我是内容");
//        bean.setCreateTime(new Date());
//        bean.setUpdateTime(LocalDateTime.now());

        list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Map<String, DemoBean> map = new HashMap<>();
            for (int j = 0; j < 2; j++) {
                int num = Integer.parseInt(i + "" + j);
                DemoBean demoBean = new DemoBean();
                demoBean.setId(num);
//                demoBean.setContext("我是内容" + num);
//                demoBean.setCreateTime(new Date());
//                demoBean.setUpdateTime(LocalDateTime.now());
                map.put(String.valueOf(num), demoBean);
            }
            list.add(map);
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
//        testJackson();
//        testGson();
        testFastJson();
    }

    static void testFastJson() {
        /**
         * ------ FastJson ------
         */

        /**
         * 简单对象
         */
        //序列化
        String simpleSerialize = JSON.toJSONString(bean);
        System.out.println(simpleSerialize);
        //反序列化
        DemoBean simpleDeserialize = JSON.parseObject(simpleSerialize, DemoBean.class);
        System.out.println(simpleDeserialize);
//        /**
//         * 复杂对象
//         */
//        //序列化
//        String complexSerialize = JSON.toJSONString(list);
//        //反序列化
//        com.alibaba.fastjson.TypeReference<List<Map<String, DemoBean>>> typeReference =
//                new com.alibaba.fastjson.TypeReference<List<Map<String, DemoBean>>>(){};
//        List<Map<String, DemoBean>> complexDeserialize = JSON.parseObject(complexSerialize, typeReference);
    }

    static void testGson() {
        /**
         * ------ Gson ------
         */

        Gson gson = new Gson();
        /**
         * 简单对象
         */
        //序列化
        String simpleSerialize = gson.toJson(bean);
        //反序列化
        DemoBean simpleDeserialize = gson.fromJson(simpleSerialize, DemoBean.class);
        System.out.println(simpleSerialize);
        System.out.println(simpleDeserialize);
//        /**
//         * 复杂对象
//         */
//        //序列化
//        String complexSerialize = gson.toJson(list);
//        //反序列化
//        Type type = new TypeToken<List<Map<String, DemoBean>>>() {}.getType();
//        List<Map<String, DemoBean>> complexDeserialize = gson.fromJson(complexSerialize, type);
    }

    public static void testJackson() throws JsonProcessingException {
        /**
         * ------ Jackson ------
         */

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        /**
         * 简单对象
         */
        //序列化
        String simpleSerialize = mapper.writeValueAsString(bean);
        System.out.println(simpleSerialize);
        //反序列化
        DemoBean simpleDeserialize = mapper.readValue(simpleSerialize, DemoBean.class);
        System.out.println(simpleDeserialize);
//        /**
//         * 复杂对象
//         */
//        //序列化
//        String complexSerialize = mapper.writeValueAsString(list);
//        //反序列化
//        com.fasterxml.jackson.core.type.TypeReference<List<Map<String, DemoBean>>> typeReference =
//                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, DemoBean>>>(){};
//        List<Map<String, DemoBean>> complexDeserialize = mapper.readValue(complexSerialize, typeReference);
    }
}

class DemoBean {
    private Integer id;
    private Boolean fresh;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean isFresh() {
        return fresh;
    }
    public Boolean getFresh() {
        return fresh;
    }

    public void setFresh(Boolean fresh) {
        this.fresh = fresh;
    }
//    private String context;
//    private Date createTime;
//    private LocalDateTime updateTime;
}
