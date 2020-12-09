package com.z.mq.common;

public interface LifeCycle {

    void startup() throws Exception;

    void shutdown() throws Exception;

    boolean isStarted();
}
