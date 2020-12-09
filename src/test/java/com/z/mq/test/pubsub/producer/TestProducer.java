/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.test.pubsub.producer;

import com.z.mq.client.AsyncSendCallback;
import com.z.mq.common.protocol.MessageModel;
import com.z.mq.common.protocol.MessageType;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.test.TestConstants;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/17
 */
public class TestProducer {

    private static final Logger logger = LoggerFactory.getLogger(TestProducer.class);

    static Producer producer = null;

    public static void main(String args[]) throws Exception {

        producer = new Producer("127.0.0.1:15565");
        boolean result = producer.create(TestConstants.diskPubSubMessageQueue, MessageType.DISK, MessageModel.PUBSUB);
        logger.info("创建Disk pubsub message queue:{}", result);
        boolean result2 = producer.create(TestConstants.fastMessageQueue,MessageType.FAST,MessageModel.PUBSUB);
        logger.info("创建Fast pubsub message queue:{}", result2);
        while (true) {
            produceMsgAsync(producer, TestConstants.diskPubSubMessageQueue,MessageType.DISK);
            Thread.sleep(3000);
            produceMsgAsync(producer, TestConstants.fastMessageQueue,MessageType.FAST);
            Thread.sleep(3000);
        }
    }


    public static void produceMsgAsync(Producer producer, String queuName,MessageType messageType) throws Exception {

        MessagePacket messagePacket = new MessagePacket();

        messagePacket.setBody(new String("produce  pubsub msg!测试::"+messageType).getBytes());
        //mq队列名称
        messagePacket.setQueueName(queuName);
        producer.sendMessage(messagePacket, new AsyncSendCallback() {
            @Override
            public void success(Object result) {
                ResponsePacket responsePacket = (ResponsePacket) result;
                logger.info("发送消息成功:{},response:{}",messageType, responsePacket);
            }
            @Override
            public void fail(Exception e) {
                logger.error("发送消息失败:{},:{}",messageType, e);
            }
        });
    }

}
