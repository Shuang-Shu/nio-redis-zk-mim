package com.mdc.mim.endecoder;

import java.util.List;
import java.util.function.Supplier;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/*
 * Use Kryo to convert byte array to object
 */
public class KryoContentEncoder extends MessageToMessageEncoder<byte[]> {

    final ThreadLocal<Kryo> serializerThreadLocal;

    public KryoContentEncoder(Supplier<Kryo> supplier) {
        serializerThreadLocal = ThreadLocal.withInitial(() -> supplier.get());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        out.add(serializerThreadLocal.get().readObject(new Input(msg), Object.class));
    }

}
