package com.mdc.mim.endecoder;

import java.util.function.Supplier;

import com.esotericsoftware.kryo.Kryo;
import com.mdc.mim.common.dto.Message.KeepAliveRequest;
import com.mdc.mim.common.dto.Message.KeepAliveResponse;
import com.mdc.mim.common.dto.Message.LoginRequest;
import com.mdc.mim.common.dto.Message.LoginResponse;
import com.mdc.mim.common.dto.Message.LogoutRequest;
import com.mdc.mim.common.dto.Message.LogoutResponse;
import com.mdc.mim.common.dto.Message.MessageNotification;
import com.mdc.mim.common.dto.Message.MessageRequest;
import com.mdc.mim.common.dto.Message.MessageResponse;
import com.mdc.mim.common.entity.Platform;
import com.mdc.mim.common.utils.ClassIdUtils;

public class Common {
    public static final int MAX_FRAME_LENGTH = 1 << 15;
    public static final int MAGIC_NUMBER = 0x66AF;
    public static final int APP_VERSION = 1;
    public static final int CONTENT_LENGTH = 4;

    private static final Class<?>[] messageClasses = {
            LoginRequest.class, LoginResponse.class, LogoutRequest.class, LogoutResponse.class,
            KeepAliveRequest.class, KeepAliveResponse.class,
            MessageRequest.class, MessageResponse.class, MessageNotification.class, Platform.class
    };

    public static final Supplier<Kryo> supplier = () -> {
        var kryo = new Kryo();
        // kryo.register(Message.class);
        for (var clazz : messageClasses) {
            kryo.register(clazz, ClassIdUtils.generateClassId(clazz, Common.APP_VERSION));
        }
        return kryo;
    };
}
