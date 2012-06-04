package com.metamx.common;

import com.metamx.common.logger.Logger;

import java.util.concurrent.Callable;

/**
 */
public class Timing {
    public static <RetType> RetType timeBenchmarkWrapException(String prefix, Callable<RetType> callable, final Logger log) {
        try {
            return timeBenchmark(prefix, callable, log);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <RetType> RetType timeBenchmark(String prefix, Callable<RetType> callable, Logger log) throws Exception {
        RetType retVal;

        long startTime = System.currentTimeMillis();
        retVal = callable.call();
        long endTime = System.currentTimeMillis();

        log.info(String.format("%s completed %,d millis.", prefix, endTime - startTime));

        return retVal;
    }
}
