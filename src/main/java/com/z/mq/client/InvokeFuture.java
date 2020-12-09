package com.z.mq.client;

import com.z.mq.common.exception.RpcException;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.protocol.ResponsePacket;
import com.z.mq.common.thread.RPCThreadPool;
import com.z.mq.config.MqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * InvokeFuture for async RPC call
 */
public class InvokeFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(InvokeFuture.class);

    private static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) RPCThreadPool.getExecutor(MqConfig.SYSTEM_PROPERTY_THREADPOOL_THREAD_NUMS,
            MqConfig.SYSTEM_PROPERTY_THREADPOOL_QUEUE_NUMS, "client");


    private MessagePacket request;
    private ResponsePacket response;
    private long startTime;
    private long responseTimeThreshold = 5000;

    private CountDownLatch countDownLatch;

    private boolean finished = false;


    private List<AsyncSendCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public InvokeFuture(MessagePacket request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public boolean isDone() {
        return finished;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        // sync.acquire(-1);
        countDownLatch.await();
        if (this.response != null) {
            if (response.isSuccess()) {
                return this.response.getPayload();
            } else {
                throw new RpcException(response.getMessage());
            }
        } else {
            logger.error("Response is null");
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        boolean success = countDownLatch.await(timeout, unit);
        if (success) {
            if (this.response != null) {
                if (response.isSuccess()) {
                    return this.response.getPayload();
                } else {
                    throw new RpcException(response.getMessage());
                }
            } else {
                logger.error("Response is null");
                return null;
            }
        } else {
            logger.warn("Timeout,Msg id:{}", request.getMsgId());
            return null;
        }
    }

    public boolean isSuccess() {
        return response != null && response.isSuccess();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(ResponsePacket response) {
        this.response = response;
        // sync.release(1);
        countDownLatch.countDown();
        finished = true;
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncSendCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public InvokeFuture addCallback(AsyncSendCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncSendCallback callback) {
        final ResponsePacket res = this.response;
        threadPoolExecutor.submit(() -> {
            if (res.isSuccess()) {
                callback.success(res);
            } else {
                callback.fail(new RpcException("Response error", new Throwable(res.getMessage())));
            }
        });
    }

    /**
     * 自定义锁
     */
    class LockLatch {

        private final Sync sync = new Sync();

        private class Sync extends AbstractQueuedSynchronizer {

            private int done = 1;
            private int pending = 0;

            protected boolean tryAcquire(int arg) {
                return getState() == done;
            }

            protected boolean tryRelease(int arg) {
                if (getState() == pending) {
                    if (compareAndSetState(pending, done)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }

            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1;
            }

            public boolean isDone() {
                return getState() == done;
            }
        }

    }
}
