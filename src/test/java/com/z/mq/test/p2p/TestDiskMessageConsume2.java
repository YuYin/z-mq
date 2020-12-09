/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.test.p2p;

import com.z.mq.consumer.Consumer;
import com.z.mq.test.TestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/17
 */
public class TestDiskMessageConsume2 {
    private static final Logger log = LoggerFactory.getLogger(TestDiskMessageConsume2.class);

    public static void main(String args[]) throws Exception {

        Consumer consumer = new Consumer("127.0.0.1:15565", TestConstants.diskP2PMessageQueue);
        consumer.registerHandler((msg, consumer1) -> log.info("收到disk p2p消息:{}",msg),(e,consumer2)->{
            log.error("disk p2p消费消息出错",e);
        });
        consumer.start();
    }
}
