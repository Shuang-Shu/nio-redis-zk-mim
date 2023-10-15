package endecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;

public class IntegerToByteBufDemo extends MessageToByteEncoder<Integer> {
    public static void main(String[] args) {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new IntegerToByteBufDemo());
            }
        };
        var channel = new EmbeddedChannel(initializer);
        channel.writeOutbound(6657);
        channel.flush();
        var buf = (ByteBuf) channel.readOutbound();
        System.out.println(buf.readInt());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) throws Exception {
        out.writeInt(msg);
    }
}
