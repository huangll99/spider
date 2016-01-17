package com.hll;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by hll on 2016/1/16.
 */
public class SpiderBootstrap {
  public static void main(String[] args) {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
    applicationContext.start();
  }
}
