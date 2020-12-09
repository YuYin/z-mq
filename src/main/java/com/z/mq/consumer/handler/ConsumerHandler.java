/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.consumer.handler;

import com.z.mq.consumer.Consumer;
import com.z.mq.consumer.MessageDto;

import java.io.IOException;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/18
 */
public interface ConsumerHandler {
      void handle(MessageDto msg, Consumer consumer) throws IOException;
}
