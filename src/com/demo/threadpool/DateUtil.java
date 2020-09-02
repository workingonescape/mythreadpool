package com.demo.threadpool;

import java.text.SimpleDateFormat;

/**
 * @author Reece Lin
 * @version 1.00
 * @time 2020/9/2 15:07
 */
public class DateUtil {


    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static volatile SimpleDateFormat simpleDateFormat ;

    private DateUtil() {

    }


    public static SimpleDateFormat getFormat(){
        if (simpleDateFormat == null) {
            synchronized (DateUtil.class) {
                if (simpleDateFormat == null) {
                    simpleDateFormat = new SimpleDateFormat(FORMAT);
                }
            }
        }
        return simpleDateFormat;
    }
}
