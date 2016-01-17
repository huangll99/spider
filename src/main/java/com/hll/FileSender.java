package com.hll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责向远端服务器发送文件
 * Created by hll on 2016/1/16.
 */
public class FileSender implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(FileSender.class);

  private static ExecutorService pool = Executors.newCachedThreadPool();
  private static NioEventLoopGroup workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

  private String remoteHost;
  private int remotePort;

  private AtomicBoolean isStoped = new AtomicBoolean(false);

  public FileSender(String host, int port) {
    this.remoteHost = host;
    this.remotePort = port;
  }

  public void stop() {
    isStoped.set(true);
  }

  public void send() {
    pool.submit(this);
  }

  @Override
  public void run() {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(workGroup)
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
      ChannelFuture future = bootstrap.connect(remoteHost, remotePort).sync();
      Channel channel = future.channel();
      writeFiles(channel);
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      logger.info("发送文件失败", e);
    } catch (IOException e) {
      logger.info("读取文件失败", e);
    }
  }

  private void writeFiles(Channel channel) throws IOException {
    listFolder(Paths.get("data"))
        .flatMap(path -> from(path))
        .subscribe(
            packet -> channel.writeAndFlush(packet),
            error -> logger.error("文件读写失败", error),
            () -> {
              channel.writeAndFlush(Packet.newEndPacket());
              logger.info("文件发送完毕！！！");
              channel.close();
            }
        );
  }


  /**
   * 列出目录下的所有文件
   */
  Observable<Path> listFolder(Path dir) {
    return Observable.<Path>create(subscriber -> {
      try {
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        subscriber.add(Subscriptions.create(() -> {
          try {
            stream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }));
        Observable.from(stream).subscribe(subscriber);
      } catch (DirectoryIteratorException ex) {
        subscriber.onError(ex);
      } catch (IOException ioe) {
        subscriber.onError(ioe);
      }
    });
  }

  Observable<Packet> from(final Path path) { // (2)
    return Observable.<Packet>create(subscriber -> {
      try {
        BufferedReader reader = Files.newBufferedReader(path);
        subscriber.add(Subscriptions.create(() -> {
          try {
            reader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }));
        //先发文件名
        subscriber.onNext(Packet.newStartPacket(path.toString().substring(path.toString().lastIndexOf("\\") + 1)));
        String line = null;
        while ((line = reader.readLine()) != null &&
            !subscriber.isUnsubscribed() && !this.isStoped.get()) {
          subscriber.onNext(Packet.newContinuePacket(line));
        }
        if (!subscriber.isUnsubscribed()) {
          subscriber.onCompleted();
        }
      } catch (IOException ioe) {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onError(ioe);
        }
      }
    });
  }

}
