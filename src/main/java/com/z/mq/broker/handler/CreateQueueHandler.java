/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker.handler;

import com.z.mq.broker.AbstractMQ;
import com.z.mq.broker.MqIoProcessor;
import com.z.mq.broker.P2PMQ;
import com.z.mq.broker.PubSubMQ;
import com.z.mq.client.Session;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessageModel;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.MessageType;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.disk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class CreateQueueHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(MqIoProcessor.class);

    private DiskQueuePool diskQueuePool;

    public CreateQueueHandler(Map<String, AbstractMQ> mqTable, DiskQueuePool diskQueuePool) {
        super(mqTable);
        this.mqTable = mqTable;
        this.diskQueuePool = diskQueuePool;
    }


    @Override
    public void handle(MessagePacket msg, Session sess) throws IOException {
        String mqName = msg.getQueueName();
        mqName = mqName.trim();
        if ("".equals(mqName)) {
            msg.setBody(new String("Missing queue name").getBytes());
            ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, "Missing queue name", "400", false);
            sess.writeAndFlush(responsePacket);
            return;
        }

        AbstractMQ mq = null;
        synchronized (mqTable) {
            mq = mqTable.get(mqName);
            if (mq != null) {
                ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, MqConstants.SUCCESS, "200", true);
                sess.writeAndFlush(responsePacket);
                return;
            }
             MessageQueue messageQueue=null;
            //持久化消息
            if (MessageType.DISK==msg.getMessageType()) {
                DiskQueue diskQueue = diskQueuePool.getDiskQueue(mqName);
                   messageQueue= new MessageDiskQueue(mqName, 1, diskQueue);
            }
            //内存消息
            else if (MessageType.FAST==msg.getMessageType()) {
                messageQueue=  new MessageMemoryQueue();
            } else {
                ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, "消息类型不支持", MqConstants.FAILED, "400", false);
                sess.writeAndFlush(responsePacket);
                return;
            }
            //P2P模式
            if (msg.getMessageModel() == MessageModel.P2P) {
                mq = new P2PMQ(mqName,messageQueue);
            }
            //发布订阅模式
            else if (msg.getMessageModel() == MessageModel.PUBSUB) {
                mq = new PubSubMQ(mqName,messageQueue);
            } else {
             ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, "消息模式不支持", MqConstants.FAILED, "400", false);
               sess.writeAndFlush(responsePacket);
                return;
            }

            mq.setCreator(sess.getRemoteAddress());

            log.info("MQ Queue Created: {}", mq);
            mqTable.put(mqName, mq);
            ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, MqConstants.SUCCESS, "200", true);
            sess.writeAndFlush(responsePacket);
        }
    }
}
