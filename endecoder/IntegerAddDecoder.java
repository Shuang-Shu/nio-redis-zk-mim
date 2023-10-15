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

public class IntegerAddDecoder extends ReplayingDecoder {
    public static void main(String[] args) {
        var initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new IntegerAddDecoder());
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

    static class PrintHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Integer i = (Integer) msg;
            System.out.println("接收到： " + i);
        }

    }

    int first;

    enum Status {
        PHASE_1, PHASE_2
    }

    public IntegerAddDecoder() {
        checkpoint(Status.PHASE_1);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch ((Status) state()) {
            case PHASE_1:
                first = in.readInt();
                checkpoint(Status.PHASE_2);
                break;
            case PHASE_2:
                out.add(first + in.readInt());
                checkpoint(Status.PHASE_1);
                break;
        }
    }
}
