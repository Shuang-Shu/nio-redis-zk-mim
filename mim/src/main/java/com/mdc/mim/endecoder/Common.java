package com.mdc.mim.endecoder;

import java.util.function.Supplier;

import com.esotericsoftware.kryo.Kryo;
import com.mdc.mim.dto.Message.KeepAliveRequest;
import com.mdc.mim.dto.Message.KeepAliveResponse;
import com.mdc.mim.dto.Message.LoginRequest;
import com.mdc.mim.dto.Message.LoginResponse;
import com.mdc.mim.dto.Message.LogoutRequest;
import com.mdc.mim.dto.Message.LogoutResponse;
import com.mdc.mim.dto.Message.MessageNotification;
import com.mdc.mim.dto.Message.MessageRequest;
import com.mdc.mim.dto.Message.MessageResponse;
import com.mdc.mim.utils.ClassIdUtils;

public class Common {
    public static final int MAX_FRAME_LENGTH = 1 << 15;
    public static final int MAGIC_NUMBER = 0x66AF;
    public static final int VERSION = 1;
    public static final int CONTENT_LENGTH = 4;

    private static final Class<?>[] messageClasses = {
            LoginRequest.class, LoginResponse.class, LogoutRequest.class, LogoutResponse.class,
            KeepAliveRequest.class, KeepAliveResponse.class,
            MessageRequest.class, MessageResponse.class, MessageNotification.class
    };

    public static final Supplier<Kryo> supplier = () -> {
        var kryo = new Kryo();
        // kryo.register(Message.class);
        for (var clazz : messageClasses) {
            kryo.register(clazz, ClassIdUtils.generateClassId(clazz, Common.VERSION));
        }
        return kryo;
    };
}
