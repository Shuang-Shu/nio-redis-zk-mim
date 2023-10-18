package com.mdc.mim;

import java.io.Serializable;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.Kryo;
import com.mdc.mim.dto.Message.LoginRequest;
import com.mdc.mim.dto.Message.LoginResponse;
import com.mdc.mim.dto.Message.LogoutRequest;
import com.mdc.mim.dto.Message.LogoutResponse;
import com.mdc.mim.dto.Message.MessageRequest;
import com.mdc.mim.dto.Message.MessageResponse;
import com.mdc.mim.endecoder.Common;
import com.mdc.mim.endecoder.KryoContentDecoder;
import com.mdc.mim.endecoder.KryoContentEncoder;
import com.mdc.mim.endecoder.MIMByteDecoder;
import com.mdc.mim.endecoder.MIMByteEncoder;
import com.mdc.mim.utils.ClassIdUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;

public class EndecoderTest {
    EmbeddedChannel channel;

    public static class Message implements Serializable {
        int id;
        String content;

        public Message() {
        }

        public Message(int id, String msg) {
            this.id = id;
            this.content = msg;
        }

        @Override
        public String toString() {
            return "msg: " + id + ", " + content;
        }
    }

    static Message message = new Message(6657, "hello world!");

    void initialChannelWith(ChannelHandler[] handlers) {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                for (var handler : handlers) {
                    ch.pipeline().addLast(handler);
                }
            }
        };
        channel = new EmbeddedChannel(initializer);
    }

    ChannelHandler[] basicBuildHandlers(final Class<?>[] classes) {
        var byteEncoder = new MIMByteEncoder();
        var byteDecoder = new MIMByteDecoder();
        Supplier<Kryo> kryoSupplier = () -> {
            var kryo = new Kryo();
            // kryo.register(Message.class);
            for (var clazz : classes) {
                kryo.register(clazz, ClassIdUtils.generateClassId(clazz, Common.VERSION));
            }
            return kryo;
        };
        var kryoContentEncoder = new KryoContentEncoder(kryoSupplier);
        var kryoContentDecoder = new KryoContentDecoder(kryoSupplier);
        return new ChannelHandler[] {
                byteDecoder, kryoContentDecoder, byteEncoder, kryoContentEncoder
        };
    }

    public void doTestPipeline(EmbeddedChannel channel, Object msg) {
        // 输出
        channel.writeOutbound(msg);
        var output = ((ByteBuf) channel.readOutbound()).slice();
        // 输入
        channel.writeInbound(output);
        var result = channel.readInbound();
        // 打印输出
        System.out.println(result);
    }

    @Test
    public void testKryoDecoder() {
        initialChannelWith(basicBuildHandlers(new Class[] { Message.class }));
        doTestPipeline(channel, message);
    }

    @Test
    public void testLoginTransport() {
        initialChannelWith(basicBuildHandlers(new Class<?>[] { LoginRequest.class, LoginResponse.class }));
        var req = LoginRequest.builder().id(123L).uid("shuangshu").appVersion("0.0.1-beta").deviceId("ios").platform(2)
                .build();
        doTestPipeline(channel, req);
        var resp = LoginResponse.builder().code(1).id(123L).info("success").expose(9).build();
        doTestPipeline(channel, resp);
    }

    @Test
    public void testLogoutTransport() {
        initialChannelWith(basicBuildHandlers(new Class<?>[] { LogoutRequest.class, LogoutResponse.class }));
        var req = LogoutRequest.builder().id(123L).build();
        var resp = LogoutResponse.builder().id(321L).build();
        doTestPipeline(channel, req);
        doTestPipeline(channel, resp);
    }

    @Test
    public void testKeepAliveTransport() {
        // TODO
    }

    @Test
    public void testMessageTransport() {
        initialChannelWith(basicBuildHandlers(new Class<?>[] { MessageRequest.class, MessageResponse.class }));
        var req = MessageRequest.builder().id(123L).from("shuangshu").to("shushuang").time(System.currentTimeMillis())
                .messageType(1).content("hello world").url("mimprotocol").property("").fromNick("shuangshu-nick")
                .json("{\"hello\": \"good\" }").build();
        var resp = MessageResponse.builder().id(321L).code(2).info("good").expose(3).lastBlock(false).blockIndex(3)
                .build();
        doTestPipeline(channel, req);
        doTestPipeline(channel, resp);
    }

}
