/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker.handler;

import com.z.mq.client.Session;

import java.io.IOException;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
public interface MessageHandler<T> {
    	void handle(T msg, Session session) throws IOException;
}
