package com.hll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Created by hll on 2016/1/16.
 */
public class PacketCodec extends ByteToMessageCodec<Packet> {
  @Override
  protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
    out.writeBytes(SerializationUtil.serialize(msg));
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    short length = in.readShort();
    byte[] bytes = new byte[length];
    in.readBytes(bytes);
    Packet packet = SerializationUtil.deserialize(bytes, Packet.class);
    out.add(packet);
  }
}
