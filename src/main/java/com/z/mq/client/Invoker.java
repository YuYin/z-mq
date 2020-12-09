/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.client;

import com.z.mq.common.protocol.MessagePacket;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
public interface Invoker {
    public abstract Object invokeSync(MessagePacket rpcRequest, final long timeoutMillis) throws Exception;

    public abstract InvokeFuture invokeWithFuture(MessagePacket rpcRequest) throws Exception;

    public abstract void oneway(MessagePacket rpcRequest) throws Exception;

    public abstract void invokeWithCallback(MessagePacket rpcRequest, AsyncSendCallback asyncRPCCallback) throws Exception;
}
