package com.demo.threadpool;

import java.util.Date;

/**
 * @author Reece Lin
 * @version 1.00
 * @time 2020/9/2 14:56
 */
public class MyTask implements Runnable {


    private int id;

    public MyTask(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        System.out.println(DateUtil.getFormat().format(new Date())+Thread.currentThread().getName()+"执行任务："+id+" 完成");
    }
}
