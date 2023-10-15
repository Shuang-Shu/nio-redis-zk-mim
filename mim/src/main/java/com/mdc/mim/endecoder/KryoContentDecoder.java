package com.mdc.mim.endecoder;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Supplier;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Use Kryo to convert object to byte array
 */
public class KryoContentDecoder extends MessageToMessageDecoder<Object> {

    final ThreadLocal<Kryo> serializerThreadLocal;

    public KryoContentDecoder(Supplier<Kryo> supplier) {
        serializerThreadLocal = ThreadLocal.withInitial(() -> supplier.get());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        var serializer = serializerThreadLocal.get();
        try (var bos = new ByteArrayOutputStream()) {
            var output = new Output(bos);
            serializer.writeObject(output, msg);
            var byteArray = output.toBytes();
            out.add(byteArray);
        }
    }

}
