package com.mdc.mim.end.session;

import com.mdc.mim.entity.User;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于保存用户信息和连接通道
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSession {
    User user;
    Channel channel;
    boolean connected;
}
