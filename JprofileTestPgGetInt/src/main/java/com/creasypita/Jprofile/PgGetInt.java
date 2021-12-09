package com.creasypita.Jprofile;

import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by lujq on 12/9/2021.
 */
public class PgGetInt {

    private static final BigInteger INTMAX = new BigInteger(Integer.toString(Integer.MAX_VALUE));
    private static final BigInteger INTMIN = new BigInteger(Integer.toString(Integer.MIN_VALUE));

    public static void main(String[] args) throws Exception {
        Thread.sleep(10000);
//        System.out.println("begin....");
//        StopWatch watch = new StopWatch();
//        watch.start();
        String s = "1.0000";

        for (int i = 0; i < 10000000; i++) {
            getInt(s);
        }
//        watch.stop();
//        System.out.println("Time Elapsed: " + watch.getTime()); // Prints: Time Elapsed: 2501
//        watch.reset();
//        watch.start();
        s = "1";
        for (int i = 0; i < 10000000; i++) {
            getIntSimple(s);
        }
//        watch.stop();
//        System.out.println("Time Elapsed: " + watch.getTime()); // Prints: Time Elapsed: 2501
        while (true){
            Thread.sleep(10000);
        }
    }

    /**
     * 用来和org.postgresql.jdbc.PgResultSet getInt方法做性能对比
     * @param s
     * @return
     */
    private static int getIntSimple(String s) {
        s = s.trim();
        return Integer.parseInt(s);
    }

    /**
     * postgres中对结果集字段转化为int类型的处理方式，具体可以查看 org.postgresql.jdbc.PgResultSet
     * 这里如果是小数，那么处理会有一定的消耗
     * @param s
     * @return
     * @throws Exception
     */
    private static int getInt(String s) throws Exception {

        if (s != null) {
            try {
                s = s.trim();
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s);
                    BigInteger i = n.toBigInteger();

                    int gt = i.compareTo(INTMAX);
                    int lt = i.compareTo(INTMIN);

                    if (gt > 0 || lt < 0) {
                        throw new Exception("Bad value for type ");
                    }
                    return i.intValue();

                } catch (NumberFormatException ne) {
                    throw new Exception("Bad value for type ");
                }
            }
        }
        return 0; // SQL NULL
    }
}
