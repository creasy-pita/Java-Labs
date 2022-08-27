package com.creasypita.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMain {
    //Logger和LoggerFactory都是日志门面jar包中的类
    private static Logger logger = LoggerFactory.getLogger(LogMain.class);

    public static void main(String[] args) {
        logger.info("hello logger");
    }
}
