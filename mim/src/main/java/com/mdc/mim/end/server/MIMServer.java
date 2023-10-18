package com.mdc.mim.end.server;

import java.io.Closeable;
import java.io.IOException;

import com.mdc.mim.end.server.handler.LoginInboundHandler;
import com.mdc.mim.end.server.handler.MessageInboundHandler;
import com.mdc.mim.endecoder.Common;
import com.mdc.mim.endecoder.KryoContentDecoder;
import com.mdc.mim.endecoder.KryoContentEncoder;
import com.mdc.mim.endecoder.MIMByteDecoder;
import com.mdc.mim.endecoder.MIMByteEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MIMServer implements Closeable {

    public MIMServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private String host;
    private int port;

    private ServerBootstrap b = new ServerBootstrap();

    public void start() {
        var bossLoopGroup = new NioEventLoopGroup(1);
        var workerLoopGroup = new NioEventLoopGroup();
        try {
            b.group(bossLoopGroup, workerLoopGroup);
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // inbouund
                    ch.pipeline().addLast(new MIMByteDecoder());
                    ch.pipeline().addLast(new KryoContentDecoder(Common.supplier));
                    ch.pipeline().addLast(new LoginInboundHandler());
                    ch.pipeline().addLast(new LoginInboundHandler());
                    ch.pipeline().addLast(new MessageInboundHandler());
                    // outbound
                    ch.pipeline().addLast(new MIMByteEncoder());
                    ch.pipeline().addLast(new KryoContentEncoder(Common.supplier));
                }
            });
            var channelFuture = b.bind(this.host, this.port).sync();
            channelFuture.channel().closeFuture().sync(); // 等待关闭的同步事件
        } catch (Exception e) {

        } finally {
            bossLoopGroup.shutdownGracefully();
            workerLoopGroup.shutdownGracefully();
        }

    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

}
