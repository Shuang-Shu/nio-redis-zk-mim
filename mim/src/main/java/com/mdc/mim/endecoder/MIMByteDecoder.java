package com.mdc.mim.endecoder;

import java.util.List;

import com.esotericsoftware.kryo.Kryo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/*
 * 将字节流转换为byte[]对象
 */
public class MIMByteDecoder extends ByteToMessageDecoder {
    static final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> new Kryo());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (in.readableBytes() < 2) {
            return;
        }
        var magicNubmer = in.readShort();
        if (magicNubmer != Common.MAGIC_NUMBER) {
            in.resetReaderIndex();
            return;
        }
        if (in.readableBytes() < 6) {
            in.resetReaderIndex();
            return;
        }
        var version = in.readShort();
        if (version > Common.APP_VERSION) {
            in.resetReaderIndex();
            return;
        }
        var length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        byte[] result = null;
        if (in.hasArray()) {
            // 堆缓冲
            result = in.slice().array();
        } else {
            // 直接缓冲
            result = new byte[length];
            in.readBytes(result, 0, length);
        }
        out.add(result); // 将解码得到的内容发送
    }

}
