package com.mdc.mim.end.client.sender;

import com.mdc.mim.end.session.ClientSession;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class BasicSender {
    ClientSession clientSession;

    public void sendMessage(Object msg) {
        if (null == getClientSession() || !getClientSession().isConnected()) {
            log.info("connection is not established yet!");
            return;
        }
        var channel = getClientSession().getChannel();
        var f = channel.writeAndFlush(msg);
        f.addListener( // 为发送事件添加回调
                new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            sendSucceed(msg);
                        } else {
                            sendFailed(msg);
                        }
                    }
                });
    }

    protected void sendSucceed(Object msg) {
        log.info("send message success: {}", msg);
    }

    protected void sendFailed(Object msg) {
        log.info("send message fail: {}", msg);
    }
}