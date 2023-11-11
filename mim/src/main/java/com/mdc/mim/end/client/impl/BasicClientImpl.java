package com.mdc.mim.end.client.impl;

import com.mdc.mim.end.client.sender.AbstractSender;

public class BasicClientImpl {
    AbstractSender sender;

    public BasicClientImpl(AbstractSender sender) {
        this.sender = sender;
        // 构建连接对象
    }

    public void loginWith(String username, String pwd) {

    }
}
