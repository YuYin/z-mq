package com.z.mq.common.thread.policy;
public interface RejectedRunnable extends Runnable {
    void rejected();
}
