/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker;

import com.z.mq.broker.handler.*;
import com.z.mq.client.Session;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.config.MqConfig;
import com.z.mq.disk.DiskQueue;
import com.z.mq.disk.DiskQueuePool;
import com.z.mq.disk.MessageDiskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
public class MqIoProcessor extends MessageIoProcessor {

    private static final Logger log = LoggerFactory.getLogger(MqIoProcessor.class);

    private  final Map<String, AbstractMQ> mqTable = new ConcurrentHashMap<String, AbstractMQ>();

    private DiskQueuePool diskQueuePool;

    private final Map<String, MessageHandler> handlerMap = new ConcurrentHashMap<String, MessageHandler>();

    public MqIoProcessor() {
        if (diskQueuePool == null) {
            diskQueuePool = new DiskQueuePool(MqConfig.storePath);
        }
        loadPersistencedMessage();
        registerHandler(MqConstants.CreateMQ, new CreateQueueHandler(mqTable,diskQueuePool));
        registerHandler(MqConstants.Produce, new ProduceHandler(mqTable));
        registerHandler(MqConstants.Consume, new ConsumeHandler(mqTable));
        registerHandler(MqConstants.HeartBeat,new HeartBeatHandler(mqTable));
    }

    public void registerHandler(String command, MessageHandler handler) {
        this.handlerMap.put(command, handler);
    }

    public  void loadPersistencedMessage() {
        log.info("加载磁盘中持久化的消息..");
        mqTable.clear();

        Map<String, DiskQueue> dqs = diskQueuePool.getQueryMap();
        for (Map.Entry<String, DiskQueue> e : dqs.entrySet()) {
            AbstractMQ mq = null;
            String name = e.getKey();
            DiskQueue diskq = e.getValue();
            int flag = diskq.getFlag();
            MessageDiskQueue queue = new MessageDiskQueue(name, diskq);
            mq = new PubSubMQ(name, queue);
            mq.setMode(flag);
            mq.lastUpdateTime = System.currentTimeMillis();
            mqTable.put(name, mq);
        }
        log.info("加载完毕!");
    }

    @Override
    public void onSessionMessage(Object obj, Session sess) throws IOException {
        MessagePacket msg = (MessagePacket) obj;
        String cmd = msg.getOperation();
        if (cmd != null) {
            MessageHandler handler = handlerMap.get(cmd);
            if (handler != null) {
                handler.handle(msg, sess);
                return;
            }
        }
        ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, "Bad format: command not support", "400", false);
        sess.write(responsePacket);
    }



}
