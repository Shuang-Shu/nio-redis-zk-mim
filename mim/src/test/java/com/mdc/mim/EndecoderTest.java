package com.mdc.mim;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import com.mdc.mim.common.Common;
import com.mdc.mim.common.dto.Message.LoginRequest;
import com.mdc.mim.common.dto.Message.LoginResponse;
import com.mdc.mim.common.dto.Message.LogoutRequest;
import com.mdc.mim.common.dto.Message.LogoutResponse;
import com.mdc.mim.common.dto.Message.MessageRequest;
import com.mdc.mim.common.dto.Message.MessageResponse;
import com.mdc.mim.common.entity.Platform;
import com.mdc.mim.endecoder.KryoContentDecoder;
import com.mdc.mim.endecoder.KryoContentEncoder;
import com.mdc.mim.endecoder.MIMByteDecoder;
import com.mdc.mim.endecoder.MIMByteEncoder;

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

    static ChannelHandler[] channelHandlers;

    static {
        var byteEncoder = new MIMByteEncoder();
        var byteDecoder = new MIMByteDecoder();
        var kryoSupplier = Common.supplier;
        var kryoContentEncoder = new KryoContentEncoder(kryoSupplier);
        var kryoContentDecoder = new KryoContentDecoder(kryoSupplier);
        channelHandlers = new ChannelHandler[] {
                byteDecoder, kryoContentDecoder, byteEncoder, kryoContentEncoder
        };
    }

    /**
     * 先将对象写入输出端，再将结果从输入端写入，检查解码结果与原始结果是否一致
     * 
     * @param channel
     * @param msg
     */
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
        initialChannelWith(channelHandlers);
        doTestPipeline(channel, message);
    }

    @Test
    public void testLoginTransport() {
        initialChannelWith(channelHandlers);
        var req = LoginRequest.builder().id(123L).uid("shuangshu").appVersion(Common.APP_VERSION).deviceId("ios")
                .platform(Platform.LINUX)
                .build();
        doTestPipeline(channel, req);
        var resp = LoginResponse.builder().code(1).id(123L).info("success").expose(9).build();
        doTestPipeline(channel, resp);
    }

    @Test
    public void testLogoutTransport() {
        initialChannelWith(channelHandlers);
        var req = LogoutRequest.builder().id(123L).build();
        var resp = LogoutResponse.builder().id(321L).build();
        doTestPipeline(channel, req);
        doTestPipeline(channel, resp);
    }

    @Test
    public void testMessageTransport() {
        initialChannelWith(channelHandlers);
        var req = MessageRequest.builder().id(123L).from("shuangshu").to("shushuang").time(System.currentTimeMillis())
                .messageType(com.mdc.mim.common.dto.Message.ChatMessageType.TEXT).content("hello world")
                .url("mimprotocol").property("").fromNick("shuangshu-nick")
                .json("{\"hello\": \"good\" }").build();
        var resp = MessageResponse.builder().id(321L).code(2).info("good").expose(3).lastBlock(false).blockIndex(3)
                .build();
        doTestPipeline(channel, req);
        doTestPipeline(channel, resp);
    }
}
