package com.neo.skill.queue;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author blue-light
 * Date 2022-09-29
 * Description
 */
@Data
@Slf4j
@Component
public class ThreadPoolQueueManager implements BeanFactoryAware {
    /**
     * 用于从IOC里取对象
     * 如果实现Runnable的类是通过spring的application.xml文件进行注入,可通过 factory.getBean()获取
     */
    private BeanFactory factory;
    /**
     * 线程池维护线程的最少数量
     */
    private final static int CORE_POOL_SIZE = 2;
    /**
     * 线程池维护线程的最大数量
     */
    private final static int MAX_POOL_SIZE = 10;
    /**
     * 线程池维护线程所允许的空闲时间
     */
    private final static int KEEP_ALIVE_TIME = 0;
    /**
     * 线程池所使用的缓冲队列大小
     */
    private final static int WORK_QUEUE_SIZE = 50;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    /**
     * 用于储存在队列中的订单,防止重复提交,在真实场景中，可用redis代替 验证重复
     */
    Map<String, Object> cacheMap = new ConcurrentHashMap<>();


    /**
     * 订单的缓冲队列,当线程池满了，则将订单存入到此缓冲队列
     */
    Queue<Object> msgQueue = new LinkedBlockingQueue<>();


    /**
     * 当线程池的容量满了，执行下面代码，将订单存入到缓冲队列
     */
    final RejectedExecutionHandler handler = (r, executor) -> {
        //订单加入到缓冲队列
        msgQueue.offer(((BusinessThread) r).getWorkgroupId());
        System.out.println("系统任务过多了, 把此线程池交给(调度线程池)逐一处理，工作组ID：" + ((BusinessThread) r).getWorkgroupId());
    };


    /**
     * 创建线程池
     */
    final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(WORK_QUEUE_SIZE),
            this.handler
    );

    /**
     * 线程池的定时任务----> 称为(调度线程池)。此线程池支持 定时以及周期性执行任务的需求。
     */
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    /**
     * 检查(调度线程池)，每秒执行一次，查看订单的缓冲队列是否有 订单记录，则重新加入到线程池
     */
    final ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(
            () -> {
                //判断缓冲队列是否存在记录
                if (!msgQueue.isEmpty()) {
                    //当线程池的队列容量少于WORK_QUEUE_SIZE，则开始把缓冲队列的订单 加入到 线程池
                    if (threadPool.getQueue().size() < WORK_QUEUE_SIZE) {
                        String workgroupId = (String) msgQueue.poll();
                        BusinessThread businessThread = new BusinessThread();
                        assert workgroupId != null;
                        businessThread.setWorkgroupId(Long.valueOf(workgroupId));
                        threadPool.execute(businessThread);
                        System.out.println("(调度线程池)缓冲队列出现发布业务，重新添加到线程池，订单号：" + workgroupId);
                    }
                }
            },
            0,
            1,
            TimeUnit.SECONDS
    );

    /**
     * 将任务加入订单线程池
     */
    public void addOrders(Long orderId) {
        log.info("当前时间: {}, 此发布请求准备添加到线程池，工作组ID: {}", LocalDateTime.now(), orderId);
        // 验证当前进入的订单是否已经存在
        if (cacheMap.get(String.valueOf(orderId)) == null) {
            cacheMap.put(String.valueOf(orderId), new Object());
            BusinessThread businessThread = new BusinessThread();
            businessThread.setWorkgroupId(orderId);
            threadPool.execute(businessThread);
        }
    }

    /**
     * 获取消息缓冲队列
     */
    public Queue<Object> getMsgQueue() {
        return msgQueue;
    }

    /**
     * 终止订单线程池+调度线程池
     */
    public void shutdown() {
        //true表示如果定时任务在执行，立即中止，false则等待任务结束后再停止
        log.info("终止发布线程池 + 调度线程池：" + scheduledFuture.cancel(false));
        scheduler.shutdown();
        threadPool.shutdown();
    }
}
