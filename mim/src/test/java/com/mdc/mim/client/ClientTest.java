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

    User user;

    @BeforeAll
    public static void initServer() throws InterruptedException {
        nettyServer = new NettyServer(host, port);
        var t1 = new Thread(() -> {
            nettyServer.start();
        });
        t1.start();
        log.info("server started");
    }

    @BeforeEach
    public void initUesr() throws InterruptedException {
        user = User.builder().uid("shuangshu-12345").devId("wsl-linux-dajfo").token("testToken")
                .nickname("ShuangShu").build();
        nettyClient.setUser(user);
        nettyClient.doConnect().sync();
        Thread.sleep(500);
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
        // 测试客户端登录功能
        nettyClient.doLogin();
        Thread.sleep(500);
    }

    /**
     * 测试消息发送功能
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSending() throws InterruptedException {
        nettyClient.doLogin().sync();
        Thread.sleep(500);
        nettyClient.doSend("test_uid_2", "test_content");
    }
}