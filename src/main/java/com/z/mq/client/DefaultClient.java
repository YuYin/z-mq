/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.client;

import com.z.mq.common.MqConstants;
import com.z.mq.common.io.IoProcessor;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.RequestMessagePacketEncoder;
import com.z.mq.common.protocol.ResponseMessagePacketDecoder;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.utils.UUIDUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
public class DefaultClient implements Client, IoProcessor {
    private static final Logger log = LoggerFactory.getLogger(DefaultClient.class);


    protected volatile ScheduledExecutorService heartbeator = null;

    protected static NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

    protected Session session;

    protected volatile ErrorHandler errorHandler;
    protected volatile ConnectedHandler connectedHandler;
    protected volatile DisconnectedHandler disconnectedHandler;
    protected CountDownLatch activeLatch = new CountDownLatch(1);
    protected int readTimeout = 3000;

    private Bootstrap bootstrap;
    private ChannelFuture channelFuture;


    protected String host;
    protected int port;

    public DefaultClient(String brokerAddress) {
        String[] bb = brokerAddress.split(":");
        if (bb.length > 2) {
            throw new IllegalArgumentException("Address invalid: " + brokerAddress);
        }
        host = bb[0].trim();
        if (bb.length > 1) {
            port = Integer.valueOf(bb[1]);
        } else {
            port = 80;
        }
        onConnected(() -> {
            log.info("Connection OK,host:{},port:{}", host, port);
        });
        onDisconnected(() -> {
            log.warn("Disconnected from,host:{},port:{}", host, port);
            ensureConnectedAsync();//automatically reconnect by default
        });
    }


    @Override
    public void connectSync(long timeout) throws IOException, InterruptedException {
        if (hasConnected()) return;
        synchronized (this) {
            if (!hasConnected()) {
                connectAsync();
                activeLatch.await(readTimeout, TimeUnit.MILLISECONDS);
                if (hasConnected()) {
                    return;
                }
                String msg = String.format("Connection to Broker(%s:%d) timeout", host, port);
                log.warn(msg);
                cleanSession();
            }
        }
    }

    private synchronized void cleanSession() throws IOException {
        if (session != null) {
            session.close();
            session = null;
            activeLatch = new CountDownLatch(1);
        }
    }

    @Override
    public void connectAsync() throws IOException {
        init();
        channelFuture = bootstrap.connect(host, port);
    }

    private void init() {
        if (bootstrap != null) return;
        bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline cp = ch.pipeline();
                        //inBound
                        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));//粘包拆包解决
                        cp.addLast(new LengthFieldPrepender(4));
                        cp.addLast(new RequestMessagePacketEncoder());
                        cp.addLast(new ResponseMessagePacketDecoder());
                        cp.addLast(new ClientHandler(DefaultClient.this));
                    }
                });

        bootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    }

    @Override
    public void ensureConnectedAsync() {

    }

    @Override
    public void ensureConnected() throws IOException, InterruptedException {

    }

    public void onError(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void onConnected(ConnectedHandler connectedHandler) {
        this.connectedHandler = connectedHandler;
    }

    public void onDisconnected(DisconnectedHandler disconnectedHandler) {
        this.disconnectedHandler = disconnectedHandler;
    }

    @Override
    public boolean hasConnected() {
        return session != null && session.isActive();
    }

    @Override
    public void onSessionCreated(Session sess) throws IOException {
        this.session = sess;
        activeLatch.countDown();
        if (connectedHandler != null) {
            connectedHandler.onConnected();
        }
    }

    @Override
    public void onSessionToDestroy(Session sess) throws IOException {
        if (this.session != null) {
            this.session.close();
            this.session = null;
        }
        if (disconnectedHandler != null) {
            disconnectedHandler.onDisconnected();
        }
    }

    @Override
    public void onSessionMessage(Object msg, Session sess) throws IOException {
        ResponsePacket message= (ResponsePacket) msg;
        log.info("Server response:{} ",message);
    }

    @Override
    public void onSessionError(Throwable e, Session sess) throws Exception {
        if (errorHandler != null) {
            errorHandler.onError(e, session);
        } else {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onSessionIdle(Session sess) throws IOException {

    }

    @Override
    public Object invokeSync(MessagePacket rpcRequest, long timeoutMillis) throws Exception {
        InvokeFuture invokeFuture = new InvokeFuture(rpcRequest);
        PendingFutureManager.pendingRPC.put(rpcRequest.getRequestId(), invokeFuture);
        doInvoke(rpcRequest);
        Object object = invokeFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
        return object;
    }

    @Override
    public InvokeFuture invokeWithFuture(MessagePacket rpcRequest) throws Exception {
        InvokeFuture invokeFuture = new InvokeFuture(rpcRequest);
        PendingFutureManager.pendingRPC.put(rpcRequest.getRequestId(), invokeFuture);
        doInvoke(rpcRequest);
        return invokeFuture;
    }

    @Override
    public void oneway(MessagePacket rpcRequest) throws Exception {
        doInvoke(rpcRequest);
    }

    @Override
    public void invokeWithCallback(MessagePacket rpcRequest, AsyncSendCallback asyncRPCCallback) throws Exception {
        InvokeFuture invokeFuture = new InvokeFuture(rpcRequest);
        invokeFuture.addCallback(asyncRPCCallback);
        PendingFutureManager.pendingRPC.put(rpcRequest.getRequestId(), invokeFuture);
        doInvoke(rpcRequest);
    }


    protected void doInvoke(MessagePacket rpcRequest) throws Exception {
        if(!hasConnected()){
			connectSync(10000);
			if(!hasConnected()){
				String msg = String.format("Connection  timeout,host:{},port:{}", host, port);
				throw new IOException(msg);
			}
		}
        session.writeAndFlush(rpcRequest);
    }

    @Override
    public void startHeartbeat(int heartbeatInSeconds) {
        if (heartbeator == null) {
            heartbeator = Executors.newSingleThreadScheduledExecutor();
            this.heartbeator.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        heartbeat();
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }, heartbeatInSeconds, heartbeatInSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopHeartbeat() {
        if (heartbeator != null) {
            heartbeator.shutdown();
        }
    }


    /**
     * 单一长连接心跳保活
     */
    @Override
    public void heartbeat() {
           if(this.hasConnected()){
			try {
				invokeSync(buildHeartBeatRequest(), 10000L);
			} catch (IOException e) {
				//ignore
			} catch (Exception e) {
              //ignore
            }
           }
    }
    protected MessagePacket buildHeartBeatRequest() {
        MessagePacket heartBeatPacket = new MessagePacket();
        heartBeatPacket.setMagicNumber(MqConstants.magic_number);
        heartBeatPacket.setRequestId(UUIDUtils.getUUID());
        heartBeatPacket.setVersion(MqConstants.version);

        heartBeatPacket.setBody(new String("heart beat!").getBytes());
        heartBeatPacket.setMsgId(UUIDUtils.getUUID());
        heartBeatPacket.setOperation(MqConstants.HeartBeat);
        return heartBeatPacket;
    }
}
