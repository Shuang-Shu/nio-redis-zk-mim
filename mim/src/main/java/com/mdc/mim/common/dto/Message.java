package com.mdc.mim.common.dto;

import com.mdc.mim.common.entity.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    MessageTypeEnum messageType; // 消息类型
    String sessionId;

    // 登入
    LoginRequest loginRequest;
    LoginResponse loginResponse;
    // 登出
    LogoutRequest logoutRequest;
    LogoutResponse logoutResponse;
    // keep-alive
    KeepAliveRequest keepAliveRequest;
    KeepAliveResponse keepAliveResponse;
    // 聊天
    MessageRequest messageRequest;
    MessageResponse messageResponse;
    // 通知
    MessageNotification messageNotification;

    public enum ChatMessageType {
        TEXT, COMPLEX
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        long id;
        String uid;
        String deviceId;
        String token;
        Platform platform;
        int appVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        long id;
        int code;
        String info;
        String sessionId;
        int expose; // 存疑
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {
        long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeepAliveRequest {
        long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeepAliveResponse {
        long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageRequest {
        long id;
        String from;
        String to;
        long time;
        Message.ChatMessageType messageType;
        String content;
        String url;
        String property;
        String fromNick;
        String json;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        long id;
        int code;
        String info;
        int expose;
        boolean lastBlock; // 是否为最后的应答
        int blockIndex; // 应答序号
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageNotification {
        long id;
        int messagType;
        boolean sender;
        String json;
        long time;
    }
}
