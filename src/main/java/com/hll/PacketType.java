package com.hll;

/**
 * Created by hll on 2016/1/16.
 */
public enum PacketType {
  START(1) , CONTINUE(2) , END(3);

  PacketType(int value) {
    this.value = value;
  }

  private int value;

  public int value() {
    return value;
  }
}
