package com.z.mq.client;

public interface AsyncSendCallback {

    void success(Object result);

    void fail(Exception e);

}
