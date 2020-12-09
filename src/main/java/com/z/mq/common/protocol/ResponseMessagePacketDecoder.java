package com.z.mq.common.protocol;

import com.z.mq.common.MqConstants;
import com.z.mq.common.serialize.RpcSerializerProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 客户端解码器
 */
public class ResponseMessagePacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ResponsePacket packet = new ResponsePacket();
        // 基础包decode
        packet.decode(in);
        // error code
        int codeLength = in.readInt();
        packet.setCode(in.readCharSequence(codeLength, MqConstants.UTF_8).toString());
        // message
        int messageLength = in.readInt();
        packet.setMessage(in.readCharSequence(messageLength, MqConstants.UTF_8).toString());

        //success
        boolean success = in.readBoolean();
        packet.setSuccess(success);

        // payload - ByteBuf实例
        int payloadLength = in.readInt();
        ByteBuf byteBuf = in.readBytes(payloadLength);
        int readableByteLength = byteBuf.readableBytes();
        byte[] bytes = new byte[readableByteLength];
        byteBuf.readBytes(bytes);
        packet.setPayload(RpcSerializerProtocol.HESSIANSERIALIZE.rpcSerializer.deserialize(bytes, null));
        byteBuf.release();
        out.add(packet);
    }
}
