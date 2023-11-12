package com.mdc.mim.end.server.handler;

import com.mdc.mim.common.dto.Message;
import com.mdc.mim.common.dto.MessageTypeEnum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatMessageRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 0 判断消息是否为Message
        if (msg == null || !(msg instanceof Message)
                || !((Message) msg).getMessageType().equals(MessageTypeEnum.MESSAGE_REQ)) {
            super.channelRead(ctx, msg);
            return;
        }
        log.debug("server receive message: {}", msg);
        var message = ((Message) msg);
        // 1 异步处理鉴权及消息转发
        doAuthenticate(message);
        doRedirectMessage(ctx, message);
    }

    private void doRedirectMessage(ChannelHandlerContext ctx, Message msg) {
    }

    private void doAuthenticate(Message msg) {
    }
}
