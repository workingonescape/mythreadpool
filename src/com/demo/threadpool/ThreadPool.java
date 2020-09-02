package com.demo.threadpool;

/**
 * @author Reece Lin
 * @version 1.00
 * @time 2020/9/2 14:05
 */
public interface ThreadPool {


    /**
     * 提交任务
     * @param command
     * @return
     */
    boolean submit(Runnable command);


    /**
     * 关闭线程池  等待任务执行完成
     */
    void  shutDown();


    /**
     * 立即关闭线程池  不会等待任务完成 直接关闭
     */
    void  shutDownNow();



}
