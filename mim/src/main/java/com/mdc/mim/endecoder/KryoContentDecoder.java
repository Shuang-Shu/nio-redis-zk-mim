package com.mdc.mim.endecoder;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Supplier;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Use Kryo to convert object to byte array
 */
public class KryoContentDecoder extends MessageToMessageDecoder<byte[]> {

    final ThreadLocal<Kryo> serializerThreadLocal;

    public KryoContentDecoder(Supplier<Kryo> supplier) {
        serializerThreadLocal = ThreadLocal.withInitial(() -> supplier.get());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        var serializer = serializerThreadLocal.get();
        Input input = null;
        try (var ios = new ByteArrayInputStream(msg)) {
            input = new Input(ios);
            int classId = input.readInt(); // 1字节版本ID+3字节全类名Hash
            var registration = serializerThreadLocal.get().getRegistration(classId);
            if (registration == null) {
                throw new IllegalStateException("class version is not registered");
            }
            @SuppressWarnings("unchecked")
            var object = serializer.readObject(input, registration.getType());
            out.add(object);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

}
