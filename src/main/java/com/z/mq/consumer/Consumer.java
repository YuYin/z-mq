/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.consumer;

import com.z.mq.client.DefaultClient;
import com.z.mq.common.MqConstants;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.thread.RPCThreadPool;
import com.z.mq.common.utils.UUIDUtils;
import com.z.mq.consumer.handler.ConsumerExceptionHandler;
import com.z.mq.consumer.handler.ConsumerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/17
 */
public class Consumer extends DefaultClient {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);


    private ConsumerHandler consumerHandler;
    private volatile ConsumerExceptionHandler consumerExceptionHandler;
    private volatile Thread consumerThread = null;
    private long consumeTimeout = 120000L; // 2 minutes
    private String queue;


    ThreadPoolExecutor executor = (ThreadPoolExecutor) RPCThreadPool.getExecutor(16, -1, "conusmerThreadPool");

    public Consumer(String brokerAddress, String queue) {
        super(brokerAddress);
        this.queue = queue;
        startHeartbeat(120);
    }



    private final Runnable consumerTask = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    final MessagePacket msg;
                    try {
                        msg = take();
                    } catch (InterruptedException e) {
                        //  Consumer.this.close();
                        break;
                    } catch (Exception e) {
                        if (consumerExceptionHandler != null) {
                            consumerExceptionHandler.onException(e, Consumer.this);
                            break;
                        }
                        throw e;
                    }
                    if (consumerHandler == null) {
                        log.warn("Missing consumerHandler, call onMessage first");
                        continue;
                    }
                    if (executor != null) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    consumerHandler.handle(toMessageDto(msg), Consumer.this);
                                } catch (IOException e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        });
                    } else {
                        try {
                            consumerHandler.handle(toMessageDto(msg), Consumer.this);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                } catch (Exception e) {
                    if (consumerExceptionHandler != null) {
                        consumerExceptionHandler.onException(e, Consumer.this);
                    } else {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    };

    private MessageDto toMessageDto(MessagePacket msg) {
        MessageDto messageDto = new MessageDto();
        messageDto.setMsgId(msg.getMsgId());
        messageDto.setQueueName(msg.getQueueName());
        messageDto.setBody(new String(msg.getBody()));
        return messageDto;
    }

    public MessagePacket take() throws Exception {
        while (true) {
            MessagePacket messagePacket = take(consumeTimeout);
            if (messagePacket == null)
                continue;
            return messagePacket;
        }
    }

    public MessagePacket take(long timeout) throws Exception {

        MessagePacket res = null;
        try {
            synchronized (this) {
                res = (MessagePacket) invokeSync(buildConsumeRequest(), timeout);
            }
            if (res == null)
                return res;
        } catch (ClosedByInterruptException e) {
            throw new InterruptedException(e.getMessage());
        } catch (IOException e) {
        }
        return res;
    }

    private MessagePacket buildConsumeRequest() {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setMagicNumber(MqConstants.magic_number);
        messagePacket.setRequestId(UUIDUtils.getUUID());
        messagePacket.setVersion(MqConstants.version);

        messagePacket.setBody(new String("consume msg!").getBytes());
        messagePacket.setMsgId(UUIDUtils.getUUID());
        //mq队列名称
        messagePacket.setQueueName(queue);
        //消费
        messagePacket.setOperation(MqConstants.Consume);
        return messagePacket;
    }


    public void registerHandler(ConsumerHandler handler, ConsumerExceptionHandler consumerExceptionHandler) throws IOException {
        onMessage(handler,consumerExceptionHandler);
    }

     public synchronized void start() {
        consumerThread = new Thread(consumerTask);
        consumerThread.setName("consumer");
        if (consumerThread.isAlive())
            return;
        consumerThread.start();
    }

    public void onMessage(final ConsumerHandler handler,final ConsumerExceptionHandler consumerExceptionHandler) throws IOException {
        this.consumerHandler = handler;
        this.consumerExceptionHandler=consumerExceptionHandler;
    }

}
