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
import com.z.mq.common.io.IoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageIoProcessor implements IoProcessor {
	private static final Logger log = LoggerFactory.getLogger(MessageIoProcessor.class);
	protected Map<String, Session> sessionTable;
	
	public MessageIoProcessor(){
		this(new ConcurrentHashMap<String, Session>());
	}
	public MessageIoProcessor(Map<String, Session> sessionTable){
		this.sessionTable = sessionTable;
/*		this.cmd(Message.HEARTBEAT, new MessageHandler() {
			public void handle(Message msg, Session sess) throws IOException { 
				//ignore
			}
		});*/
	}
	@Override
	public void onSessionCreated(Session sess) throws IOException {
		log.info("Session Created: " + sess);
		sessionTable.put(sess.id(), sess);
	}

	@Override
	public void onSessionToDestroy(Session sess) throws IOException {
		log.info("Session Destroyed: " + sess);
		cleanSession(sess);
	}

	@Override
	public void onSessionMessage(Object msg, Session sess) throws IOException {

	}

	@Override
	public void onSessionError(Throwable e, Session sess) throws Exception {
		log.info("Session Error: " + sess);
		cleanSession(sess);
	} 

	@Override
	public void onSessionIdle(Session sess) throws IOException { 
		log.info("Session Idle Remove: " + sess);
		cleanSession(sess);
	}
	
	protected void cleanSession(Session sess) throws IOException {
		try{
			sess.close();
		} finally {
			sessionTable.remove(sess.id());
		} 
	}
}

