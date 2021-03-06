/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.broker;

import com.z.mq.common.AbstractLifeCycle;
import com.z.mq.common.exception.LifeCycleException;
import com.z.mq.config.MqConfig;
import com.z.mq.common.thread.RPCThreadPool;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/7/28
 */
public abstract class AbstractRemoteServer extends AbstractLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRemoteServer.class);

    protected static int threadNums = MqConfig.SYSTEM_PROPERTY_THREADPOOL_THREAD_NUMS;
    protected static int queueNums = MqConfig.SYSTEM_PROPERTY_THREADPOOL_QUEUE_NUMS;

    protected String ip;
    protected  int port;


    protected EventLoopGroup bossGroup = null;
    protected EventLoopGroup workerGroup = null;
    protected MqIoProcessor mqIoProcessor;

    protected static ThreadPoolExecutor threadPoolExecutor;

      @Override
       public void startup() throws Exception {
          super.startup();
          try {
              beforeStart();
             doStart();
             logger.info("服务端broker启动成功,端口号: {}", port);
             afterStarted();

          }catch (Throwable t){
              shutdown();
              throw new IllegalStateException("ERROR: Failed to start the Server!", t);
          }
       }
       public void shutdown() throws Exception{
          super.shutdown();
          if(!doStop()){
             throw new LifeCycleException("server doStop fail");
          }
       }

    protected abstract void beforeStart() throws Exception;
    protected abstract boolean doStart() throws Exception;
    protected abstract void afterStarted() ;
    protected abstract boolean doStop() throws Exception;

   

    protected void initThreadPool() {
        if (threadPoolExecutor == null) {
            synchronized (AbstractRemoteServer.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = (ThreadPoolExecutor) RPCThreadPool.getExecutor(threadNums, queueNums, "server");
                }
            }
        }
    }
}
