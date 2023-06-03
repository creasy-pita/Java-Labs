package com.creasypita.logger;

import org.apache.log4j.Logger;

/**
 * Created by lujq on 6/3/2023.
 */
public class A {
    private static final Logger logger = Logger.getLogger(A.class);
    public void output(){
        logger.debug("Debug log");
        logger.info("Info log");
        logger.warn("Warn log");
        logger.error("Error log");
        logger.fatal("Fatal log");
    }
}
