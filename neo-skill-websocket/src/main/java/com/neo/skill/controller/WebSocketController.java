package com.neo.skill.controller;

import com.neo.skill.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author blue-light
 * Date 2022-10-08
 * Description
 */
@RestController
@RequestMapping("/open/socket")
public class WebSocketController {

    @Value("${mySocket.myPwd}")
    public String myPwd;

    @Resource
    private WebSocketServer webSocketServer;

    /**
     * 手机客户端请求接口
     *
     * @param id  发生异常的设备ID
     * @param pwd 密码（实际开发记得加密）
     * @throws IOException
     */
    @PostMapping(value = "/onReceive")
    public void onReceive(String id, String pwd) throws IOException {
        //密码校验一致（这里举例，实际开发还要有个密码加密的校验的），则进行群发
        if (pwd.equals(myPwd)) {
            webSocketServer.broadCastInfo(id);
        }
    }
}
