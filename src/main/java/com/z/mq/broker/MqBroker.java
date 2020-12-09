/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker;

import com.z.mq.common.protocol.RequestMessagePacketDecoder;
import com.z.mq.common.protocol.ResponseMessagePacketEncoder;
import com.z.mq.common.serialize.RpcSerializerProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/7/21
 */
public class MqBroker extends AbstractRemoteServer {

    private static final Logger logger = LoggerFactory.getLogger(MqBroker.class);


    private RpcSerializerProtocol rpcSerializerProtocol=RpcSerializerProtocol.HESSIANSERIALIZE;



    public MqBroker(String ip, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(String.format(
                    "Illegal port value: %d, which should between 0 and 65535.", port));
        }
        this.ip = ip;
        this.port = port;
    }


    @Override
    protected void beforeStart() throws Exception {
        super.initThreadPool();
    }

    @Override
    public boolean doStart() throws Exception {
        if (bossGroup == null && workerGroup == null) {
           mqIoProcessor =new MqIoProcessor();
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new RequestMessagePacketDecoder())
                                    .addLast(new ResponseMessagePacketEncoder(rpcSerializerProtocol.rpcSerializer))
                                    .addLast(new ServerHandler(mqIoProcessor));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);


            ChannelFuture future = bootstrap.bind(ip, port).sync();
            return future.isSuccess();
        }
        return false;
    }

    @Override
    public void afterStarted() {
    }

    @Override
    public boolean doStop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        return true;
    }

}
