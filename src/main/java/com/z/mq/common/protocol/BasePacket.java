package com.z.mq.common.protocol;

import com.google.common.collect.Maps;
import com.z.mq.common.MqConstants;
import com.z.mq.common.utils.ByteBufferUtils;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class BasePacket implements Serializable {

    /**
     * 魔数
     */
    protected int magicNumber;

    /**
     * 版本号
     */
    protected int version;

    /**
     * 请求唯一ID
     */
    protected String requestId;
    /**
     * 扩展header
     */
    private Map<String, String> header = new HashMap<>();

    public void put(String key, String value) {
        header.put(key, value);
    }

    public String get(String key) {
        return header.get(key);
    }


    /**
     * 基础包encode
     *
     * @param out out
     */
    public void encode(ByteBuf out) {
        // 魔数
        out.writeInt(getMagicNumber());
        // 版本
        out.writeInt(getVersion());
        ByteBufferUtils.INSTANCE.encodeUtf8CharSequence(out, getRequestId());
        // header size
        Map<String, String> header = getHeader();
        out.writeInt(header.size());
        header.forEach((k, v) -> {
            out.writeInt(k.length());
            out.writeCharSequence(k, MqConstants.UTF_8);
            out.writeInt(v.length());
            out.writeCharSequence(v, MqConstants.UTF_8);
        });
    }

    /**
     * 基础包decode
     *
     * @param in in
     */
    public void decode(ByteBuf in) {
        // 魔数
        setMagicNumber(in.readInt());
        // 版本
        setVersion(in.readInt());
        // 流水号
        int serialNumberLength = in.readInt();
        setRequestId(in.readCharSequence(serialNumberLength, MqConstants.UTF_8).toString());

        // header
        Map<String, String> attachments = Maps.newHashMap();
        setHeader(attachments);
        int attachmentSize = in.readInt();
        if (attachmentSize > 0) {
            for (int i = 0; i < attachmentSize; i++) {
                int keyLength = in.readInt();
                String key = in.readCharSequence(keyLength, MqConstants.UTF_8).toString();
                int valueLength = in.readInt();
                String value = in.readCharSequence(valueLength, MqConstants.UTF_8).toString();
                attachments.put(key, value);
            }
        }
    }
}
