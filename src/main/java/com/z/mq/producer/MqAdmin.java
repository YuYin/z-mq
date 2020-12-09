/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.producer;

import com.z.mq.client.DefaultClient;
import com.z.mq.client.InvokeFuture;
import com.z.mq.client.PendingFutureManager;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessageModel;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.MessageType;
import com.z.mq.common.utils.UUIDUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class MqAdmin extends DefaultClient {


    public MqAdmin(String brokerAddress) {
        super(brokerAddress);
    }

    public boolean create(String queueName, MessageType messageType, MessageModel messageModel) throws Exception {
        return invokeSync(buildCreateMQMessage(queueName, messageType,messageModel), 10000L);
    }

    public boolean create(String queueName) throws Exception {
        return invokeSync(buildCreateMQMessage(queueName, MessageType.DISK,MessageModel.PUBSUB), 10000L);
    }

    @Override
    public Boolean invokeSync(MessagePacket rpcRequest, long timeoutMillis) throws Exception {
        InvokeFuture invokeFuture = new InvokeFuture(rpcRequest);
        PendingFutureManager.pendingRPC.put(rpcRequest.getRequestId(), invokeFuture);
        super.doInvoke(rpcRequest);
        invokeFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
        return invokeFuture.isSuccess();
    }


    public MessagePacket queryMq(String queueName) throws Exception {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setOperation(MqConstants.QueryMQ);
        messagePacket.setQueueName(queueName);
        return (MessagePacket) super.invokeSync(messagePacket, 10000L);
    }

    public boolean deleteMq() {

        return true;
    }

    private MessagePacket buildCreateMQMessage(String queueName, MessageType messageType,MessageModel messageModel) {
        MessagePacket messagePacket = new MessagePacket();
        buildCommonField(messagePacket);
        messagePacket.setOperation(MqConstants.CreateMQ);
        messagePacket.setQueueName(queueName);
        messagePacket.setMessageType(messageType);
        messagePacket.setMessageModel(messageModel);
        return messagePacket;
    }

    protected void buildCommonField(MessagePacket messagePacket) {
        messagePacket.setMagicNumber(MqConstants.magic_number);
        messagePacket.setVersion(MqConstants.version);
        messagePacket.setRequestId(UUIDUtils.getUUID());
    }
}
