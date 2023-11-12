package com.mdc.mim.end.client.sender;

import java.util.concurrent.atomic.AtomicLong;

import com.mdc.mim.common.entity.User;
import com.mdc.mim.end.session.ClientSession;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class AbstractSender {
    ClientSession clientSession;
    protected static AtomicLong atomicId = new AtomicLong();

    protected static long getId() {
        return atomicId.getAndIncrement();
    }

    /*
     * 发送消息
     */
    public ChannelFuture sendMessage(Object msg) {
        if (null == getClientSession() || !getClientSession().isConnected()) {
            log.info("connection is not established yet!");
            return null;
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
                            log.error(future.cause().getMessage());
                            sendFailed(msg);
                        }
                    }
                });
        return f;
    }

    protected void sendSucceed(Object msg) {
        log.info("successfully sending message");
        log.debug("successfully sending message: {}", msg);
    }

    protected void sendFailed(Object msg) {
        log.info("send message failed: {}", msg);
    }

    protected User getUser() {
        return clientSession.getUser();
    }

}
