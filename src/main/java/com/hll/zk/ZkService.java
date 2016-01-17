package com.hll.zk;

import com.hll.ConfigConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * zk的client初始化，注册节点等
 * Created by hll on 2016/1/16.
 */
public class ZkService {

  private Logger logger = LoggerFactory.getLogger(ZkService.class);

  private final CuratorFramework client;

  public ZkService(String registryAddress, String namespace) {
    client = CuratorFrameworkFactory.builder()
        .namespace(namespace)
        .connectString(registryAddress)
        .sessionTimeoutMs(5000)
        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
        .build();
    client.start();
  }

  public CuratorFramework getClient() {
    return client;
  }


  /**
   * 注册节点
   * @param serverId
   * @param host
   * @param port
   */
  public void register(String serverId, String host, int port) {
    try {
      byte[] bytes = (host + ":" + port).getBytes("UTF-8");
      client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ConfigConstant.ZK_SERVER_PATH + "/" + serverId,bytes);
    } catch (UnsupportedEncodingException e) {
      logger.error("字符串编解码错误：" ,e);
    } catch (Exception e) {
      logger.error("Zk注册服务失败：",e);
    }
  }

}
