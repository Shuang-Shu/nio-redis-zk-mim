package com.mdc.mim.end.session;

import com.mdc.mim.common.dto.Message;
import com.mdc.mim.common.entity.User;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 胶水类，用于保存用户信息和连接通道，同时记录连接状态，
 * 该类同时还与确定的通道绑定，负责相互信息的交换
 * **注意：** 该类不建立连接，但负责关闭连接
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSession {
    public static final AttributeKey<ClientSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");
    private User user;
    private String sessionId;
    private boolean hasLogined;
    private Channel channel;
    private boolean connected = false;

    public ClientSession(Channel channel, User user) {
        this.channel = channel;
        this.user = user;
        this.connected = true;
        // 将ClientSession绑定到channel
        this.channel.attr(SESSION_KEY).set(this);
    }

    public void bindChannel() {
        channel.attr(SESSION_KEY).set(this);
    }

    public ChannelFuture writeAndFlush(Object pojo) {
        return channel.writeAndFlush(pojo);
    }

    public void writeAndClose(Object pojo) {
        var cf = channel.writeAndFlush(pojo);
        cf.addListener(ChannelFutureListener.CLOSE); // 添加关闭Listener
    }

    public void loginSuccess(Message message) {
        this.sessionId = message.getSessionId(); // 获取sessionId
        this.hasLogined = true;
    }

    public void close() {
        connected = false;

        var cf = channel.closeFuture();
        cf.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (cf.isSuccess()) {
                    log.info("has closed session");
                }
            }

        });
    }
}
