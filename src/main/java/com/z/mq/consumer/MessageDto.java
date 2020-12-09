/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.consumer;

import com.z.mq.common.protocol.BasePacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageDto extends BasePacket {
	//数据
	private String body;
	//消息id
	private String msgId;
	//queue
	private String queueName;

}
