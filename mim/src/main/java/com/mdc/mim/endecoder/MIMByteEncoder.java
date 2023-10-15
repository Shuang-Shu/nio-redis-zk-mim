package com.mdc.mim.endecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MIMByteEncoder extends MessageToByteEncoder<byte[]> {

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        var length = msg.length;
        out.writeShort(Common.MAGIC_NUMBER);
        out.writeShort(Common.VERSION);
        out.writeInt(length);
        out.writeBytes(msg);
    }

}
