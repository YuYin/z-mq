package com.z.mq.common.protocol;

import com.z.mq.common.utils.ByteBufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

/**
 * 客户端编码器
 */
@RequiredArgsConstructor
public class RequestMessagePacketEncoder extends MessageToByteEncoder<MessagePacket> {


    @Override
    protected void encode(ChannelHandlerContext context, MessagePacket packet, ByteBuf out) throws Exception {
        // 基础包encode
        packet.encode(out);

        if (packet.getBody() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(packet.getBody().length);
            out.writeBytes(packet.getBody());
        }
        if (packet.getMsgId() == null) {
            out.writeInt(0);
        } else {
            ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, packet.getMsgId());
        }
        if (packet.getOperation() == null) {
            out.writeInt(0);
        } else {
            ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, packet.getOperation());
        }
        if (packet.getQueueName() == null) {
            out.writeInt(0);
        } else {
            ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, packet.getQueueName());
        }
        if (packet.getMessageType() == null) {
            out.writeInt(0);
        } else {
            ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, packet.getMessageType().getType());
        }
        if (packet.getMessageModel() == null) {
            out.writeInt(0);
        } else {
            ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, packet.getMessageModel().getModel());
        }


    }
}
