/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker.handler;

import com.z.mq.broker.AbstractMQ;
import com.z.mq.client.Session;
import com.z.mq.common.protocol.MessagePacket;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public class ConsumeHandler extends BaseHandler  {

    public ConsumeHandler(Map<String, AbstractMQ> mqTable) {
        super(mqTable);
    }

    @Override
    public void handle(MessagePacket msg, Session sess) throws IOException {
        	AbstractMQ mq = findMQ(msg, sess);
			if(mq == null) return;
			mq.consume(msg, sess);
    }
}
