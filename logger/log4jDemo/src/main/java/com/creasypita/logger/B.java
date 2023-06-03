package com.creasypita.logger;

import org.apache.log4j.Logger;

/**
 * Created by lujq on 6/3/2023.
 */
public class B {
    private static final Logger logger = Logger.getLogger(B.class);
    public void output(){
        logger.debug("Debug log");
        logger.info("Info log");
        logger.warn("Warn log");
        logger.error("Error log");
        logger.fatal("Fatal log");
    }
}
