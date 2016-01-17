package com.hll.zk;

import com.hll.FileReceiveHandler;
import com.hll.Packet;
import com.hll.PacketCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.Test;

/**
 * Created by hll on 2016/1/16.
 */
public class NettyTest {
  @Test
  public void server(){
    NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    NioEventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline()
                  .addLast(new LengthFieldBasedFrameDecoder(1024*1024,0,2))
                  .addLast(new PacketCodec())
                  .addLast(new FileReceiveHandler());
            }
          });
      ChannelFuture channelFuture = bootstrap.bind("127.0.0.1", 8888).sync();
      channelFuture.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  @Test
  public void client(){
    NioEventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(workerGroup)
        .option(ChannelOption.TCP_NODELAY, true)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline()
                .addLast(new LengthFieldPrepender(2))
                .addLast(new PacketCodec());
          }
        });
    try {
      ChannelFuture future = bootstrap.connect("127.0.0.1", 8888).sync();
      Channel channel = future.channel();
      channel.writeAndFlush(new Packet(1,"what is app"));
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }finally {
      workerGroup.shutdownGracefully();
    }
  }
}
