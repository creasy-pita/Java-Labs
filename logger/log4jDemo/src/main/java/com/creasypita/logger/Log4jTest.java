package com.creasypita.logger;
import org.apache.log4j.Logger;
/**
 * Created by lujq on 3/7/2023.
 */
public class Log4jTest {
    // 创建 logger 实例
    private static final Logger logger = Logger.getLogger(Log4jTest.class);

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            logger.debug("Debug log");
            logger.info("Info log");
            logger.warn("Warn log");
            logger.error("Error log");
            logger.fatal("Fatal log");
        }
    }
}
