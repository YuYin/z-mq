package com.z.mq.common.protocol;

import com.z.mq.common.MqConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 服务端解码器
 */
@RequiredArgsConstructor
public class RequestMessagePacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> list) throws Exception {
        MessagePacket packet = new MessagePacket();
        // 基础包decode
        packet.decode(in);

        int bodyLength = in.readInt();
        if (bodyLength > 0) {
            //解码byte[]数组数据
            ByteBuf byteBuf = in.readBytes(bodyLength);
            int readableByteLength = byteBuf.readableBytes();
            byte[] bytes = new byte[readableByteLength];
            byteBuf.readBytes(bytes);
            packet.setBody(bytes);
        }

        int idLength = in.readInt();
        if (idLength > 0) {
            packet.setMsgId(in.readCharSequence(idLength, MqConstants.UTF_8).toString());
        }

        int operationLength = in.readInt();
        if (operationLength > 0) {
            packet.setOperation(in.readCharSequence(operationLength, MqConstants.UTF_8).toString());
        }

        int queueNameLength = in.readInt();
        if (queueNameLength > 0) {
            packet.setQueueName(in.readCharSequence(queueNameLength, MqConstants.UTF_8).toString());
        }
        int messageTypeLength = in.readInt();
        if (messageTypeLength > 0) {
            packet.setMessageType(MessageType.fromValue(in.readCharSequence(messageTypeLength, MqConstants.UTF_8).toString()));
        }
        int messageModelLength = in.readInt();
        if (messageModelLength > 0) {
            packet.setMessageModel(MessageModel.fromValue(in.readCharSequence(messageModelLength, MqConstants.UTF_8).toString()));
        }
        list.add(packet);
    }
}
