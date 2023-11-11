package com.mdc.mim.common.entity;

import com.esotericsoftware.minlog.Log;
import com.mdc.mim.common.dto.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;
    private String devId;
    private String token; // 基于JWT的访问token
    private String nickname;
    @Builder.Default
    private Platform platform = Platform.LINUX; // 用户使用平台

    public static User parseFromLoginMsg(Message.LoginRequest req) {
        Log.info("parsing user from loginRequest message");
        return User.builder().uid(req.getUid()).devId(req.getDeviceId()).token(req.getToken())
                .platform(req.getPlatform()).build();
    }
}
