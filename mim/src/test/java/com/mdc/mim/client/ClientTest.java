package com.mdc.mim.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mdc.mim.common.entity.User;
import com.mdc.mim.end.client.NettyClient;
import com.mdc.mim.end.server.NettyServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class ClientTest {

    static NettyServer nettyServer;
    static String host = "0.0.0.0";
    static int port = 8080;

    @Autowired
    NettyClient nettyClient;

    @BeforeAll
    public static void initServer() {
        nettyServer = new NettyServer(host, port);
        new Thread(() -> {
            nettyServer.start();
        }).start();
        log.info("server started");
    }

    @BeforeEach
    public void initUesr() {
        var user = User.builder().uid("shuangshu-12345").devId("wsl-linux-dajfo").token("testToken")
                .nickname("ShuangShu").build();
        nettyClient.setUser(user);
    }

    @Test
    public void basicTest() {
        Assertions.assertNotNull(nettyClient);
        Assertions.assertNotNull(nettyClient.getUser());
    }

    @Test
    public void testConnect() throws InterruptedException {
        nettyClient.doConnect();
        Thread.sleep(1000); // waiting for connectings
        Assertions.assertEquals(true, nettyClient.getClientSession().isConnected());
    }

    @Test
    public void testLogin() throws InterruptedException {
        nettyClient.doConnect();
        Thread.sleep(500);
        // 测试登录功能
        nettyClient.doLogin();
        Thread.sleep(500);
    }
}
