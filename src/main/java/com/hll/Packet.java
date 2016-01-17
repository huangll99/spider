package com.hll;

/**
 * Created by hll on 2016/1/16.
 */
public class Packet {

  private int type;
  private String content;

  public Packet(int type, String content) {
    this.type = type;
    this.content = content;
  }

  public static Packet newStartPacket(String filename){
    return new Packet(PacketType.START.value(),filename);
  }

  public static Packet newContinuePacket(String line){
    return new Packet(PacketType.CONTINUE.value(),line);
  }

  public static Packet newEndPacket(){
    return new Packet(PacketType.END.value(),"END!!!");
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "Packet{" +
        "type=" + type +
        ", content='" + content + '\'' +
        '}';
  }
}
