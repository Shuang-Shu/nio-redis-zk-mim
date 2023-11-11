package com.mdc.mim;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

public class BufTest {

    static Charset utf8 = Charset.forName("UTF-8");

    @Test
    public void testBuf() {
        var cbuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        var headBuf = Unpooled.copiedBuffer("test1", utf8);
        var bodyBuf = Unpooled.copiedBuffer("test", utf8);
        cbuf.addComponents(headBuf, bodyBuf);
        sendMsg(cbuf);
        cbuf.release();
    }

    private void sendMsg(CompositeByteBuf cbuf) {
        for (var b : cbuf) {
            int len = b.readableBytes();
            byte[] arr = new byte[len];
            b.getBytes(b.readerIndex(), arr);
            System.out.println(new String(arr, utf8));
        }
    }
}
