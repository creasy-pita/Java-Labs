package com.creasypita.resttemplate.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by lujq on 3/1/2024.
 */
public class RestTemplateUtil {

//    private static Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    //    public static RestTemplate restTemplate = SpringContextHolder.getBean("restTemplate");
    public static RestTemplate restTemplate = new RestTemplate();
    /**
     * 发起rest请求并携带中台token
     * @param serviceUrl
     * @param method
     * @return ResponseEntityWrapper
     * @throws ResourceAccessException
     */
    public static ResponseEntityWrapper<String> exchangeByInnerToken(String serviceUrl, HttpMethod method) throws ResourceAccessException {
        return exchangeByInnerToken(serviceUrl, method, String.class);
    }

    /**
     * 发起rest请求并携带中台token
     * @param serviceUrl
     * @param method
     * @param responseType
     * @param <T>
     * @return ResponseEntityWrapper
     * @throws ResourceAccessException
     */
    public static <T> ResponseEntityWrapper<T> exchangeByInnerToken(String serviceUrl, HttpMethod method, Class<T> responseType) throws ResourceAccessException {
        return exchangeByInnerToken(serviceUrl, method, null, null, responseType);
    }

    /***
     * @param serviceUrl 服务地址
     * @param method
     * @return
     */
    public static <T> ResponseEntityWrapper<T> exchangeByInnerToken(String serviceUrl, HttpMethod method, MultiValueMap<String, Object> filesMap, Class<T> responseType) throws ResourceAccessException {
        return exchangeByInnerToken(serviceUrl, method, filesMap, null, responseType);
    }

    /**
     *
     * @param serviceUrl url
     * @param method  httpmethod
     * @param filesMap  文件的部分
     * @param formParams  uri参数
     * @param responseType  返回类型
     * @param <T>
     * @return
     * @throws ResourceAccessException
     */
    public static <T> ResponseEntityWrapper<T> exchangeByInnerToken(String serviceUrl, HttpMethod method, MultiValueMap<String, Object> filesMap, Map<String, Object> formParams, Class<T> responseType) throws ResourceAccessException {
//        if(logger.isDebugEnabled()){
//            logger.debug("调用服务：{}", serviceUrl);
//        }
        HttpEntity<MultiValueMap<String, Object>> request = getMultiValueMap(new HttpHeaders(), filesMap, formParams);
        ResponseEntityWrapper<T> entityWrapper = new ResponseEntityWrapper<>();
        try {
            entityWrapper.setResponseEntity(restTemplate.exchange(serviceUrl, method, request, responseType));
        } catch (HttpStatusCodeException e) {
            entityWrapper.setResponseEntity(ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders()).body(null));
            entityWrapper.setErrorMsg(e.getResponseBodyAsString());
        }
//        if(logger.isDebugEnabled()){
//            ResponseEntity<T> responseEntity = entityWrapper.getResponseEntity();
//            logger.debug("返回状态码：{}", responseEntity.getStatusCode());
//            logger.debug("返回报文：{}", responseEntity.getBody());
//            if (responseEntity.getStatusCode() != HttpStatus.OK) {
//                logger.debug("错误信息：{}", entityWrapper.getErrorMsg());
//            }
//        }
        return entityWrapper;
    }

    /**
     *
     * @param headers
     * @param filesMap  body中的文件
     * @param formParams body表单参数列表
     * @return
     */
    private static HttpEntity<MultiValueMap<String, Object>> getMultiValueMap(HttpHeaders headers, MultiValueMap<String, Object> filesMap, Map<String, Object> formParams) {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        if (filesMap != null && filesMap.size() > 0) {
            headers.set("Content-Type", "multipart/form-data");
            multiValueMap.addAll(filesMap);
        } else {
            headers.set("Content-Type", "application/x-www-form-urlencoded");
        }
        if (formParams != null && formParams.size() > 0) {
            for (Map.Entry<String, Object> entry : formParams.entrySet()) {
                multiValueMap.add(entry.getKey(), entry.getValue());
            }
        }
        return new HttpEntity<>(multiValueMap, headers);
    }
}
