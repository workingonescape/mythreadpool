package com.demo.threadpool;

/**
 * @author Reece Lin
 * @version 1.00
 * @time 2020/9/2 14:58
 */
public class Test {


    public static void main(String[] args) {
        MyThreadPool threadPool = new MyThreadPool(2, 6, 20);

        int times = 100;

        for (int i = 0; i < times; i++) {
            threadPool.submit(new MyTask(i));
        }

        threadPool.shutDown();
    }
}
