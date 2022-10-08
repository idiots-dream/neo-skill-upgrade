package com.neo.skill.controller;

import com.neo.skill.queue.ThreadPoolQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author blue-light
 * Date 2022-09-29
 * Description
 */
@Slf4j
@RestController
public class TestController {
    @Resource
    ThreadPoolQueueManager queueManager;

    @GetMapping("/{id}")
    public String test(@PathVariable Long id) {
        queueManager.addOrders(id);
        return "success";
    }
}
