package com.mdc.mim.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.mdc.mim.common.entity.User;
import com.mdc.mim.end.client.NettyClient;

@ComponentScan("com.mdc.mim.end")
@SpringBootApplication
public class ClientApp {
    public static void main(String[] args) {
        var context = SpringApplication.run(ClientApp.class, args);
        var nettyClient = context.getBean(NettyClient.class);
        var user = User.builder().uid("shuangshu-12345").devId("wsl-linux-dajfo").token("testToken")
                .nickname("ShuangShu").build();
        nettyClient.setUser(user);
        nettyClient.doConnect();
        System.out.println(nettyClient);
    }
}
