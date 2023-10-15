package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;

public class HandlerDemo {
    static class TestHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            var buf = (ByteBuf) msg;
            var bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            System.out.println("testHandler read: " + new String(bytes));
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("testHandler read complete");
            super.channelReadComplete(ctx);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("testHandler is added");
        }
    }

    public static void main(String[] args) {
        var initializer = new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel ch) throws Exception {
                ch.pipeline().addLast(new TestHandler());
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(initializer);
        // var byteBuf = ByteBufAllocator.DEFAULT.buffer(2,
        // 16);
        var byteBuf = Unpooled.directBuffer();
        byteBuf.writeBytes("hello world".getBytes());
        channel.writeInbound(byteBuf);
        channel.flush();
        channel.close();
    }
}
