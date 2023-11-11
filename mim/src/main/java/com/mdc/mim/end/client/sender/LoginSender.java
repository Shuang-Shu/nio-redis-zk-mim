package com.mdc.mim.end.client.sender;

import org.springframework.stereotype.Service;

import com.mdc.mim.common.dto.Message;
import com.mdc.mim.common.entity.User;
import com.mdc.mim.endecoder.Common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("loginSender")
public class LoginSender extends AbstractSender {
    public void sendLogin(User user) {
        var loginReq = Message.LoginRequest.builder().uid(user.getUid()).deviceId(user.getDevId())
                .token(user.getToken()).platform(user.getPlatform()).appVersion(Common.APP_VERSION).build();
        log.info("sending log request");
        super.sendMessage(loginReq);
    }
}
