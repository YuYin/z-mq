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
import com.z.mq.disk.MessageQueue;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractMQ implements Closeable{	
	protected final String queue;
	protected int mode;
	public long lastUpdateTime = System.currentTimeMillis();
	
	protected final MessageQueue msgQ;
	
	public AbstractMQ(String queue, MessageQueue msgQ) {
		this.msgQ = msgQ;
		this.queue = queue;
	}

	public String getQueue() {
		return queue;
	}

	public void produce(MessagePacket msg, Session sess) throws IOException {
		msgQ.offer(msg); 
		dispatch();
	}
 
	public abstract void consume(MessagePacket msg, Session sess) throws IOException;

	abstract void dispatch() throws IOException; 
	
	public abstract void cleanSession(Session sess);
	
	public abstract void cleanSession();


	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}    
	
	public String getCreator() {
		return msgQ.getCreator();
	}

	public void setCreator(String creator) {
		this.msgQ.setCreator(creator);
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
}
