/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.z.mq.broker;

import com.z.mq.client.Session;
import com.z.mq.common.protocol.MessagePacket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class PullSession { 
	Session session;
    MessagePacket pullMessagePacket;
   
    final ReentrantLock lock = new ReentrantLock();
    //todo fix me
	//发布订阅模式,将持久化队列里面的数据拷贝到每个消费者队列，可能会造成内存爆增
	final BlockingQueue<MessagePacket> msgQ = new LinkedBlockingQueue<MessagePacket>();
	
	public PullSession(Session sess, MessagePacket pullMessagePacket) {
		this.session = sess;
		this.setPullMessagePacket(pullMessagePacket);
	}
	

	public void setPullMessage(MessagePacket msg) {
		this.lock.lock();
		this.pullMessagePacket = msg;
		this.lock.unlock();
	} 

	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public MessagePacket getPullMessagePacket() {
		return this.pullMessagePacket;
	}
	
	public void setPullMessagePacket(MessagePacket msg) {
		this.lock.lock();
		this.pullMessagePacket = msg;
		if(msg == null){
			this.lock.unlock();
			return; 
		}
		this.lock.unlock();
	} 
	

	public BlockingQueue<MessagePacket> getMsgQ() {
		return msgQ;
	}
	

}

