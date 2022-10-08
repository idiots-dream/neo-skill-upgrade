package com.neo.skill.queue;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author blue-light
 * Date 2022-09-29
 * Description
 */
@Data
@Slf4j
@Component
@Scope("prototype")
public class BusinessThread implements Runnable {
    private Long workgroupId;

    @Override
    public void run() {
        // 业务操作
        log.info("当前时间: {}, 多线程已经处理系统配置发布请求，工作组ID: {}", LocalDateTime.now(), workgroupId);
        // 线程阻塞
        try {
            Thread.sleep(1000 * 60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
