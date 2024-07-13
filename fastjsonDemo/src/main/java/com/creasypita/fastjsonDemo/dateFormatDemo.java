package com.creasypita.fastjsonDemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lujq on 7/13/2024.
 * 切换不同的日期格式
 */
public class dateFormatDemo {


    public static void main(String[] args) throws IOException {
//        date_parse_error();
        fastjson_json_parse_date_from_string();
    }

    /**
     * 模拟 客户端 日期类型格式化输出字符串；服务端 日期字符串反序列化为日期的操作
     * 客户端使用 使用JSON.toJSONString 序列化日期值
     * 服务端使用 ObjectMapper.readValue 反序列化出日期
     * 前者日期格式为长日期，后者为短日期时会报序列化失败
     *
     * @throws IOException
     */
    public static void date_parse_error() throws IOException {

        //1 客户端调用接口时日期值转化为string  com.alibaba.fastjson.JSON date转化到string

        // 设置全局默认日期格式
        String jsonDateFormatLong = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String jsonDateFormatShort = "yyyy-MM-dd HH:mm:ss";

        // 配置JSON序列化器
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(java.util.Date.class, new SimpleDateFormatSerializer(jsonDateFormatLong));

        // 创建日期对象
        Date date = new java.util.Date();

        // 序列化日期对象
        String jsonString = JSON.toJSONString(date, serializeConfig, SerializerFeature.WriteDateUseDateFormat);
        System.out.println(jsonString);


        //2 服务端接收string转化为日志  com.fasterxml.jackson.databind.ObjectMapper 将string 转化为日期

        // 创建ObjectMapper实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 配置日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat(jsonDateFormatShort);
        objectMapper.setDateFormat(dateFormat);

//        // 序列化日期对象
//        Date date = new Date();
//        String jsonString = objectMapper.writeValueAsString(date);
//        System.out.println(jsonString);

        // 反序列化日期对象
        Date parsedDate = objectMapper.readValue(jsonString, Date.class);
        System.out.println(parsedDate);

    }

    /**
     * 测试 可以对不同长短日期直接反序列化出日志类型
     */
    public static void fastjson_json_parse_date_from_string(){
        Date parsedDate1 = JSON.parseObject("\"2024-07-13T18:19:20.173+0800\"", java.util.Date.class);
        Date parsedDate2 = JSON.parseObject("\"2024-07-13 18:19:20\"", java.util.Date.class);
    }


    public static void main2(String[] args) {
        // 设置全局默认日期格式
        String jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        // 配置JSON序列化器
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(java.util.Date.class, new SimpleDateFormatSerializer(jsonDateFormat));

        // 创建日期对象
        java.util.Date date = new java.util.Date();

        // 序列化日期对象
        String jsonString = JSON.toJSONString(date, serializeConfig, SerializerFeature.WriteDateUseDateFormat);
        System.out.println(jsonString);

        // 反序列化日期对象
        java.util.Date parsedDate = JSON.parseObject(jsonString, java.util.Date.class);
        System.out.println(parsedDate);


        //切换到其他格式
        jsonDateFormat="yyyy-MM-dd HH:mm:ss";
        serializeConfig.put(java.util.Date.class, new SimpleDateFormatSerializer(jsonDateFormat));
        jsonString = JSON.toJSONString(date, serializeConfig, SerializerFeature.WriteDateUseDateFormat);
        System.out.println(jsonString);

        // 反序列化日期对象
//        parsedDate = JSON.parseObject(jsonString, java.util.Date.class);
        parsedDate = JSON.parseObject("\"2024-07-13T18:19:20.173+0800\"", java.util.Date.class);
        System.out.println(parsedDate);
    }
}
