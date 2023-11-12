package com.mdc.mim.end.server.handler;

import java.util.UUID;

import com.mdc.mim.common.dto.Message;
import com.mdc.mim.common.dto.MessageTypeEnum;
import com.mdc.mim.common.dto.ResponsesCodeEnum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 0 判断消息是否为Message
        if (msg == null || !(msg instanceof Message)
                || !((Message) msg).getMessageType().equals(MessageTypeEnum.LOGIN_REQ)) {
            super.channelRead(ctx, msg);
            return;
        }
        log.debug("server receive message: {}", msg);
        // 1 异步处理认证流程
        doIdentify(ctx, msg);
    }

    /**
     * 认证，将sessionId返回给client
     * 
     * @return
     */
    private boolean doIdentify(ChannelHandlerContext ctx, Object msg) {
        var loginReq = (Message) msg;
        var loginResp = Message.builder().messageType(MessageTypeEnum.LOGIN_RESP).loginResponse(
                Message.LoginResponse.builder().id(loginReq.getLoginRequest().getId()).sessionId(getSessionId())
                        .code(ResponsesCodeEnum.SUCCESS.getCode())
                        .info("successfully identified").expose(0).build())
                .build();
        var f = ctx.channel().writeAndFlush((Object) loginResp);
        f.addListener(
                new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            log.info("successfully response client");
                        } else {
                            future.cause().printStackTrace();
                        }
                    }

                });
        return false;
    }

    private String getSessionId() {
        return UUID.randomUUID().toString();
    }
}
