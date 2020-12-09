/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.test.pubsub.producer;

import com.z.mq.client.AsyncSendCallback;
import com.z.mq.common.protocol.MessageModel;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.MessageType;
import com.z.mq.producer.Producer;
import com.z.mq.test.TestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/17
 */
public class TestProduceDelayMessage {

    private static final Logger logger = LoggerFactory.getLogger(TestProduceDelayMessage.class);

    static Producer producer = null;

    public static void main(String args[]) throws Exception {

        producer = new Producer("127.0.0.1:15565");
        boolean result = producer.create(TestConstants.diskPubSubMessageQueue +"-delay", MessageType.DISK, MessageModel.PUBSUB);
        logger.info("创建delay消息 Disk message queue:{}", result);
        produceDelayMsgAsync(producer, TestConstants.diskPubSubMessageQueue,MessageType.DISK, "10s");
        while (!Thread.currentThread().isInterrupted()){
            TimeUnit.HOURS.sleep(1);
        }
    }

    /**
     * 生产延迟消息,延迟10s
     * @param producer
     * @param queuName
     * @param messageType
     * @param delayTime
     * @throws Exception
     */
    public static void produceDelayMsgAsync(Producer producer, String queuName,MessageType messageType,String delayTime) throws Exception {

        MessagePacket messagePacket = new MessagePacket();

        messagePacket.setBody(new String("produce msg!测试::delay::"+messageType).getBytes());
        //mq队列名称
        messagePacket.setQueueName(queuName);
        messagePacket.put("delay",delayTime);
        producer.sendMessage(messagePacket, new AsyncSendCallback() {
            @Override
            public void success(Object result) {
                ResponsePacket responsePacket = (ResponsePacket) result;
                logger.info("发送delay消息成功:{},response:{}",messageType, responsePacket);
            }
            @Override
            public void fail(Exception e) {
                logger.error("发送消息失败:{},:{}",messageType, e);
            }
        });
    }

}
