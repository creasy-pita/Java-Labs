package com.creasypita.resttemplate.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by lujq on 2/28/2024.
 * ResponseEntity包装类
 * 用途：resttemplate发起的请求如果发生异常，也可以用此类把通信码（发生异常时通信码值是非200）和错误信息记录下来，返回给外层使用
 */
public class ResponseEntityWrapper<T> {

    private ResponseEntity<T> responseEntity;
    /**
     * 用于保存通信码非200时的responsebody信息或者异常信息
     */
    private String errorMsg;

    public ResponseEntity<T> getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity<T> responseEntity) {
        this.responseEntity = responseEntity;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public HttpStatus getStatusCode() {
        return this.responseEntity != null ? this.responseEntity.getStatusCode() : null;
    }

    /**
     * 用于保存通信码200时的responsebody信息
     * @return
     */
    public T getBody() {
        return this.responseEntity != null ? this.responseEntity.getBody() : null;
    }

    public HttpHeaders getHeaders() {
        return this.responseEntity != null ? this.responseEntity.getHeaders() : null;
    }

}

