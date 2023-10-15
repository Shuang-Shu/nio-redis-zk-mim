package endecoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class LengthFieldBasedFrameDecoderDemo {
    static final short VERSION = 12;
    static final short MAGICCODE = 6657;

    public static void main(String[] args) throws InterruptedException {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(128, 2, 4, 4, 10));
                ch.pipeline().addLast(new StringPrinter());
            }
        };
        var channel = new EmbeddedChannel(initializer);
        var buf = Unpooled.buffer(128);
        for (int i = 0; i < 10; i++) {
            String data = "helloworld! (" + i + ")";
            buf.writeShort(VERSION); // version
            buf.writeInt(data.getBytes().length); // length
            buf.writeInt(MAGICCODE); // magic number
            buf.writeBytes(data.getBytes());
            channel.writeInbound(
                    buf);
        }
        Thread.sleep(1000);
    }

    public static class StringPrinter extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            var buf = (ByteBuf) msg;
            var data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            System.out.println(new String(data));
        }
    }
}
