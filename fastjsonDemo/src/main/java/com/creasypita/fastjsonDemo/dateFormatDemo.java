package com.creasypita.fastjsonDemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

/**
 * Created by lujq on 7/13/2024.
 * 切换不同的日期格式
 */
public class dateFormatDemo {
    public static void main(String[] args) {
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
    }
}
