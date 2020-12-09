package com.z.mq.broker; /**
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


import com.z.mq.client.Session;
import com.z.mq.common.protocol.MessagePacket;
import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.disk.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 点对点模式，一条消息只能被一个消费者消费
 */
public class P2PMQ extends AbstractMQ {
	private static final Logger log = LoggerFactory.getLogger(P2PMQ.class);

	protected final Map<String, Session> pullSessions = new ConcurrentHashMap<String, Session>();

	protected final BlockingQueue<PullSession> pullQ = new LinkedBlockingQueue<PullSession>();

	public P2PMQ(String name, MessageQueue msgQ) {
		super(name, msgQ);
	}

	@Override
	public void consume(MessagePacket msg, Session sess) throws IOException {
		if(!pullSessions.containsKey(sess.id())){
			pullSessions.put(sess.id(), sess);
		}
		for(PullSession pull : pullQ){
			if(pull.getSession() == sess){
				pull.setPullMessagePacket(msg);
				this.dispatch();
				return;
			}
		}

		PullSession pull = new PullSession(sess, msg);
		pullQ.offer(pull);
		this.dispatch();
	}

	void dispatch() throws IOException{
		while(pullQ.peek() != null && msgQ.size() > 0){
			MessagePacket msg = null;
			PullSession pull = null;
			synchronized (this) {
				pull = pullQ.peek();
				if(pull == null){
					continue;
				}
				if( !pull.getSession().isActive() ){
					pullQ.poll();
					continue;
				}

				msg = msgQ.poll();
				if(msg == null){
					break;
				}
				pull = pullQ.poll();
			}

			this.lastUpdateTime = System.currentTimeMillis();
			try {
				MessagePacket pullMsg = pull.getPullMessagePacket();
				pull.getSession().write(ResponseUtils.buildResponse(pullMsg,msg,"success","200",true));
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				msgQ.offer(msg);
			}
		}
	}

	@Override
	public void cleanSession(Session sess) {
		pullSessions.remove(sess.id());

		Iterator<PullSession> iter = pullQ.iterator();
		while(iter.hasNext()){
			PullSession pull = iter.next();
			if(sess == pull.session){
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void cleanSession() {
		Iterator<PullSession> iter = pullQ.iterator();
		while(iter.hasNext()){
			PullSession pull = iter.next();
			if(!pull.session.isActive()){
				pullSessions.remove(pull.session.id());
				iter.remove();
			}
		}
	}

	@Override
	public String toString() {
		return "MQ [queue=" + queue + ", creator=" + getCreator() + "]";
	}

	public int consumerOnlineCount(){
		return pullSessions.size();
	}

	@Override
	public void close() throws IOException {
		PullSession pull = null;
		while( (pull = pullQ.poll()) != null){
			try{
				pull.session.close();
			}catch(IOException e){
				log.warn(e.getMessage(), e);
			}
		}
	}

}
