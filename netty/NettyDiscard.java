package netty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class NettyDiscard {
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        new Thread(
                () -> {
                    new NettyDiscard(8080).start();
                }).start();
        Thread.sleep(1000);
        var cli = new Socket("localhost", 8080);
        var sos = cli.getOutputStream();
        sos.write("hello world!".getBytes());
        sos.close();
        cli.close();
    }

    final ServerBootstrap b = new ServerBootstrap();
    final int port;

    public NettyDiscard(int port) {
        this.port = port;
    }

    void start() {
        var bossLoopGroup = new NioEventLoopGroup(1);
        var workerLoopGroup = new NioEventLoopGroup();
        try {
            // 设置线程组
            b.group(bossLoopGroup, workerLoopGroup);
            // 设置通道
            b.channel(NioServerSocketChannel.class);
            // 设置监听端口
            b.localAddress(port);
            // 配置选项
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            // 设置子通道的处理流水线
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettyDiscardHandler());
                }
            });
            // 同步等待端口绑定
            var cf = b.bind().sync();
            System.out.println("server bind: " + port);
            // 同步等待channel的关闭事件
            cf.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }
    }

    class NettyDiscardHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            var in = (ByteBuf) msg;
            try {
                var bos = new ByteArrayOutputStream();
                while (in.isReadable()) {
                    bos.write(in.readByte());
                }
                System.out.println("收到消息，打印并丢弃: " + new String(bos.toByteArray()));
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
