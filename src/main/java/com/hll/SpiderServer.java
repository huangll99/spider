package com.hll;

import com.hll.zk.ZkService;
import io.netty.bootstrap.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 节点启动，开始监听端口，提供服务
 * Created by hll on 2015/12/15.
 */
public class SpiderServer implements InitializingBean{

  private Logger logger = LoggerFactory.getLogger(SpiderServer.class);

  private NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
  private NioEventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

  private String host;
  private int port;
  private ZkService zkService;
  private String serverId;

  public SpiderServer(String serverId,String host,int port,ZkService zkService){
    this.serverId=serverId;
    this.host = host;
    this.port = port;
    this.zkService = zkService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
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
      ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
      logger.info("SpiderServer is started!!!");
      zkService.register(serverId,host,port);
      channelFuture.channel().closeFuture().sync();
    }finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
