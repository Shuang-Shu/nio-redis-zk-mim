package endecoder;

import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageDecoder;

public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {

    public static void main(String[] args) {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new IntegerAddDecoder());
                ch.pipeline().addLast(new IntegerToStringDecoder());
                ch.pipeline().addLast(new PrintStringHandler());
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

    @Override
    protected void decode(ChannelHandlerContext ctx, Integer msg, List<Object> out) throws Exception {
        out.add(String.valueOf(msg));
    }

    public static class PrintStringHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("接收到String： " + msg);
        }

    }
}
