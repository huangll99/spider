package com.hll.zk;

import com.hll.ConfigConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.UnsupportedEncodingException;

/**
 * ZkService Tester.
 */
public class ZkServiceTest {

  private CuratorFramework client;

  @Before
  public void before() throws Exception {
    client = CuratorFrameworkFactory.builder()
        .namespace("spider")
        .connectString("127.0.0.1:2181")
        .sessionTimeoutMs(5000)
        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
        .build();
    client.start();
  }

  @After
  public void after() throws Exception {
  }

  /**
   * Method: register(String serverId, String host, int port)
   */
  @Test
  public void testRegister() throws Exception {
    byte[] bytes = new byte[0];
    try {
      bytes = ("127.0.0.1" + ":" + "8888").getBytes("UTF-8");
      client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ConfigConstant.ZK_SERVER_PATH + "/" + "server-1",bytes);

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

} 
