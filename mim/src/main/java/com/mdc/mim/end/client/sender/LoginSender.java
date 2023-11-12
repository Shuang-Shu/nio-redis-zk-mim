package com.mdc.mim.end.client.sender;

import org.springframework.stereotype.Service;

import com.mdc.mim.common.Common;
import com.mdc.mim.common.dto.Message;
import com.mdc.mim.common.dto.MessageTypeEnum;
import com.mdc.mim.common.entity.User;

import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("loginSender")
public class LoginSender extends AbstractSender {
    public ChannelFuture sendLogin(User user) {
        var loginRequest = Message.LoginRequest.builder().id(getId()).uid(user.getUid()).deviceId(user.getDevId())
                .token(user.getToken()).platform(user.getPlatform()).appVersion(Common.APP_VERSION).build();
        var msg = Message.builder().messageType(MessageTypeEnum.LOGIN_REQ).loginRequest(loginRequest).build();
        log.debug("sending log request: {}", msg);
        return super.sendMessage(msg);
    }
}
