package com.demo.threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Reece Lin
 * @version 1.00
 * @time 2020/9/2 14:06
 */
public class MyThreadPool implements ThreadPool {


    /**
     * 当前线程数
     */
    private int currentNum ;


    /**
     * 核心线程数
     */
    private int coreSize;


    /**
     * 最大核心线程数
     */
    private static final int MAX_CORE_SIZE = 3;


    /**
     * 线程池中最大线程数
     */
    private int maxSize;


    /**
     * 最大线程数
     */
    private static final int MAX_SIZE = 6;


    /**
     * 用于存储添加进线程池的任务
     */
    private BlockingQueue<Runnable>  queue;


    private int queueSize;

    /**
     * 设置队列的最大值，方便后边模拟拒绝策略
     */
    private static final int MAX_QUEUE_SIZE = 20;


    /**
     * 用于存储核心线程和非核心线程的队列
     */
    private List<Worker> workers;


    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 线程池是否正在运行  使用volatile修饰 以便其他线程可以即使看到
     */
    private volatile boolean isRunning;



    public MyThreadPool(int coreSize, int maxSize, int queueSize) {
        this.coreSize = Math.min(coreSize, MAX_CORE_SIZE);
        this.maxSize = Math.min(maxSize, MAX_SIZE);
        this.queueSize = Math.min(queueSize, MAX_QUEUE_SIZE);
        this.queue = new LinkedBlockingQueue<>(this.queueSize);
        this.workers = Collections.synchronizedList(new ArrayList<>(this.maxSize));
        this.isRunning = true;
        //初始化线程池 主要是核心线程的生成
        initThreadPool();
    }

    /**
     * 初始化线程池 只生产核心线程数个线程放入 workers
     */
    private void initThreadPool() {
        for (int i = 0; i < this.coreSize; i++) {
            Worker worker = new Worker("核心线程-" + i);
            //添加进workers
            workers.add(worker);
            //启动核心线程
            worker.start();
            //更新当前线程池线程数目
            currentNum++;
            System.out.println(DateUtil.getFormat().format(new Date())+"核心线程-" + i + "启动完成，等待执行任务");
        }
    }


    @Override
    public boolean submit(Runnable command) {
        if (isRunning) {
            //若是核心线程数还没达到最大，则新建核心线程执行任务
            if (currentNum < MAX_CORE_SIZE) {
                String threadName = "新建核心线程-" + ++this.coreSize;
                Worker worker = new Worker(threadName, command);
                workers.add(worker);
                worker.start();
                currentNum++;
                return true;
            } else if (currentNum < MAX_SIZE) {
                //若是队列未满，则直接添加进队列
                if (queue.offer(command)) {
                    return true;
                    //若是队列已满，则创建非核心线程去执行任务
                } else {
                    String threadName = "非核心线程-" + (currentNum - MAX_CORE_SIZE);
                    Worker worker = new Worker(threadName, command);
                    workers.add(worker);
                    worker.start();
                    currentNum++;
                    return true;

                }
                //若是线程数已到最大，且队列也满了，则直接执行拒绝策略
            } else if (currentNum >= MAX_SIZE && !queue.offer(command)) {
                System.out.println(DateUtil.getFormat().format(new Date())+" 线程池已满负载运行，拒绝了该任务,当前任务队列大小为："+queue.size());
                return false;
            }
        }
        return false;
    }



    /**
     * 关闭线程池 但不是马上关闭 等待任务执行完成
     */
    @Override
    public void shutDown() {

        //若是队列中还有未执行的任务 则进入休眠 休眠1s 等待任务执行完成
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isRunning = false;

        for (Worker worker : workers) {
            //使用interrupt来中断线程
            worker.interrupt();
            worker = null;
        }

        //情况queue
        queue.clear();
        workers.clear();
    }


    /**
     * 直接关闭
     */
    @Override
    public void shutDownNow() {
        for (Worker worker : workers) {
            worker.interrupt();
            worker = null;
        }
        queue.clear();
        workers.clear();
    }




    /**
     * worker 真正负责执行任务的线程
     */
    private class Worker extends Thread {


        private Runnable command;


        public Worker(String name) {
            super(name);
        }

        public Worker(String name, Runnable command) {
            super(name);
            this.command = command;
        }

        @Override
        public void run() {
            //当线程池正在运行 或者 存储任务的队列不为空时
            while (isRunning || !queue.isEmpty()) {
                if (command != null) {
                    command.run();
                    command = null;//help gc
                }else {
                    //此时线程池被关闭 但是队列中还有任务时，就要把剩余的任务执行完毕
                    command = queue.poll();//此时会阻塞，直到有任务被放进队列
                    if (command != null) {
                        command.run();
                        command = null;//help gc
                    }
                }
            }
        }
    }


    private class MyThreadPoolHandler extends ThreadPoolExecutor.AbortPolicy {



        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new MyRejectException(DateUtil.getFormat().format(new Date()) + "当前线程池已满，拒绝了该任务");
        }

    }

    private class MyRejectException extends RuntimeException {


        public MyRejectException(String message) {
            super(message);
        }


    }

}
