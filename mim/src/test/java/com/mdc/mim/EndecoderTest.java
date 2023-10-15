package com.mdc.mim;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.kryo.Kryo;
import com.mdc.mim.endecoder.KryoContentDecoder;
import com.mdc.mim.endecoder.KryoContentEncoder;
import com.mdc.mim.endecoder.MIMByteDecoder;
import com.mdc.mim.endecoder.MIMByteEncoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;

public class EndecoderTest {
    EmbeddedChannel channel;

    static class Message {
        int id;
        String content;

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

    void initialWith(ChannelHandler[] handlers) {
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

    @Test
    public void testKryoDecoder() {
        var byteEncoder = new MIMByteEncoder();
        var byteDecoder = new MIMByteDecoder();
        Supplier<Kryo> kryoSupplier = () -> {
            var kryo = new Kryo();
            kryo.register(Message.class);
            return kryo;
        };
        var kryoContentEncoder = new KryoContentEncoder(kryoSupplier);
        var kryoContentDecoder = new KryoContentDecoder(kryoSupplier);
        ChannelHandler[] handlers = {
                byteDecoder, kryoContentDecoder, byteEncoder, kryoContentEncoder
        };
        initialWith(handlers);
        channel.writeOutbound(message);
        var output = channel.readOutbound();
        System.out.println(output);
    }
}
