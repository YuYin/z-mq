/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.test;

import com.z.mq.broker.MqBroker;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/17
 */
public class TestMqBroker {

    public static void main(String args[]) throws Exception {
        MqBroker mqBroker =new MqBroker("127.0.0.1",15565);
        mqBroker.startup();
        while (!Thread.currentThread().isInterrupted()){
            TimeUnit.HOURS.sleep(1);
        }
    }
}
