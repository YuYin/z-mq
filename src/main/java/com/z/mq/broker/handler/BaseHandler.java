/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker.handler;

import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.broker.AbstractMQ;
import com.z.mq.client.Session;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.ResponsePacket;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public abstract class BaseHandler implements MessageHandler<MessagePacket> {

    protected Map<String, AbstractMQ> mqTable;

    public BaseHandler(Map<String, AbstractMQ> mqTable) {
        this.mqTable = mqTable;
    }

    protected AbstractMQ findMQ(MessagePacket msg, Session sess) throws IOException {
        String mqName = msg.getQueueName();
        AbstractMQ mq = mqTable.get(mqName);
        if (mq == null) {
            ResponsePacket responsePacket = ResponseUtils.buildResponse(msg, null, " Queue not found", "404", false);
            sess.writeAndFlush(responsePacket);
            return null;
        }
        return mq;
    }
}
