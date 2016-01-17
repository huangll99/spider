package com.hll;

import com.hll.zk.ZkService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * server节点上下线监听
 * Created by hll on 2015/12/15.
 */
public class SpiderFileService implements InitializingBean {

  private Logger logger = LoggerFactory.getLogger(SpiderFileService.class);

  private ConcurrentHashMap<String,FileSender> serverMap = new ConcurrentHashMap();

  private ZkService zkService;
  private String serverId;

  public SpiderFileService(String serverId, ZkService zkService) {
    this.serverId = serverId;
    this.zkService = zkService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    //注册监听
    PathChildrenCache cache = new PathChildrenCache(zkService.getClient(), ConfigConstant.ZK_SERVER_PATH, true);
    cache.start();
    cache.getListenable().addListener(
        (client, event) -> {
          switch (event.getType()) {
            case CHILD_ADDED:
              String addServer = event.getData().getPath();
              if (isBigger(serverId,addServer.substring(addServer.lastIndexOf("/")+1))){
                logger.info("发现id比本机小的节点，发文件，id:"+addServer);
                //获取ip和端口
                byte[] data = event.getData().getData();
                String s = new String(data, "UTF-8");
                String[] split = s.split(":");
                String remoteHost = split[0];
                int port = Integer.parseInt(split[1]);

                //发送文件
                FileSender sender = new FileSender(remoteHost, port);
                serverMap.put(addServer,sender);
                sender.send();
              }
              break;
            case CHILD_REMOVED:
              String delServer = event.getData().getPath();
              if (isBigger(serverId,delServer.substring(delServer.lastIndexOf("/")+1))){
                logger.info("节点:"+delServer+"下线，停止发文件");
                //停止发文件
                serverMap.get(delServer).stop();
                serverMap.remove(delServer);
              }
              break;
            default:
              break;
          }
        }
    );
  }

  private boolean isBigger(String s1,String s2){
    return Integer.parseInt(s1.split("-")[1]) > Integer.parseInt(s2.split("-")[1]);
  }
}
