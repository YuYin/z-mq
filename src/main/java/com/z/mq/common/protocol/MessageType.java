/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.common.protocol;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/24
 */
public enum MessageType {
    FAST("fast"),
    DISK("disk");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static MessageType fromValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.getType().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("value = %s", value));
    }
}