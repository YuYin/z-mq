/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker.handler;

import com.z.mq.broker.AbstractMQ;
import com.z.mq.client.Session;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.broker.MqIoProcessor;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.utils.TimeKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class ProduceHandler extends BaseHandler {
    private static final Logger log = LoggerFactory.getLogger(MqIoProcessor.class);

    public ProduceHandler(Map<String, AbstractMQ> mqTable) {
        super(mqTable);
    }

    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(16);


    @Override
    public void handle(MessagePacket msg, Session session) throws IOException {
        final AbstractMQ mq = findMQ(msg, session);
        if (mq == null) return;
        //是否延迟消息
        String delay = msg.get("delay");
        if (delay != null) {
            long value = TimeKit.parseDelayTime(delay);
            if (value > 0) {
                timer.schedule(() -> {
                    try {
                        mq.produce(msg, session);
                         log.info("delay msg saved successfully!");
                        mq.lastUpdateTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }, value, TimeUnit.MILLISECONDS);
                ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, MqConstants.SUCCESS, "200", true);
                session.writeAndFlush(responsePacket);
                return;
            }
        }
        mq.produce(msg, session);
        ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, MqConstants.SUCCESS, "200", true);
        session.writeAndFlush(responsePacket);
        log.info("msg saved successfully!");
    }
}
