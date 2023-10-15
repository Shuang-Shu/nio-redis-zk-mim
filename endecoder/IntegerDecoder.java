package endecoder;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ReplayingDecoder;

public class IntegerDecoder extends ReplayingDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int i = in.readInt();
        System.out.println("解码为：" + i);
        out.add(i);
    }

    public static void main(String[] args) {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new IntegerDecoder());
                ch.pipeline().addLast(new PrintHandler());
            }
        };
        var channel = new EmbeddedChannel(initializer);
        var buf = Unpooled.buffer();
        for (int i = 0; i < 10; i++) {
            buf.retain();
            buf.writeInt(i);
            channel.writeInbound(buf);
            buf.clear();
        }
        channel.close();
    }

    public static class PrintHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Integer i = (Integer) msg;
            System.out.println("接收到： " + i);
        }

    }
}
