/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.common.protocol;

import lombok.Data;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
//@EqualsAndHashCode(callSuper = true)
@Data
public class MessagePacket extends BasePacket {
	//数据
	private byte[] body;
	//消息id
	private String msgId;
	//操作类型
	private String operation;
	//队列名
	private String queueName;
	//消息类型:内存消息/持久化消息
	private MessageType messageType;
	//消息模式
	private MessageModel messageModel;

	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"[body="+new String(body)+",msgId="+msgId+",operation="+operation
				+",queueName="+queueName/*+",messageType="+(messageType!=null?messageType.getType():null)
				+",messageModel="+(messageModel!=null?messageModel.getModel():null)*/+",requestId="+requestId+"]";
	}

}
