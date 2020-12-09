/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.common.protocol;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/24
 */
public enum MessageModel {
    P2P("p2p"),
    PUBSUB("pubsub");

    private String model;

    MessageModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return this.model;
    }

    public static MessageModel fromValue(String modelName) {
        for (MessageModel model : MessageModel.values()) {
            if (model.getModel().equals(modelName) ) {
                return model;
            }
        }
        throw new IllegalArgumentException(String.format("value = %s", modelName));
    }
}