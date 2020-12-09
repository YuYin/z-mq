/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.producer;

import com.z.mq.client.AsyncSendCallback;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.utils.UUIDUtils;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class Producer extends MqAdmin {

    public Producer(String brokerAddress) {
        super(brokerAddress);
         startHeartbeat(120);
    }

    public Boolean sendMessage(MessagePacket messagePacket, final long timeoutMillis) throws Exception {
        buildMsgField(messagePacket);
        return super.invokeSync(messagePacket, timeoutMillis);
    }

    public Boolean sendMessage(MessagePacket messagePacket) throws Exception {
        buildMsgField(messagePacket);
        return super.invokeSync(messagePacket, 10000L);
    }
    
     public void sendMessage(MessagePacket messagePacket, AsyncSendCallback asyncRPCCallback) throws Exception {
        buildMsgField(messagePacket);
         super.invokeWithCallback(messagePacket, asyncRPCCallback);
    }

    private void buildMsgField(MessagePacket messagePacket) {
        buildCommonField(messagePacket);
        messagePacket.setOperation(MqConstants.Produce);
        messagePacket.setMsgId(UUIDUtils.getUUID());
    }


}
