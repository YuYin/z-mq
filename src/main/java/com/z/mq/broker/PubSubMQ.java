/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
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
import com.z.mq.common.utils.ResponseUtils;
import com.z.mq.disk.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 发布订阅模式,一条消息被一组消费者订阅了相同消息主题的消费者消费
 */
public class PubSubMQ extends AbstractMQ {
    private static final Logger log = LoggerFactory.getLogger(PubSubMQ.class);

    protected final ConcurrentMap<String, PullSession> pullMap = new ConcurrentHashMap<String, PullSession>();


    public PubSubMQ(String name, MessageQueue msgQ) {
        super(name, msgQ);
    }

    @Override
    public void consume(MessagePacket msg, Session sess) throws IOException {
        PullSession pull = pullMap.get(sess.id());
        if (pull != null) {
            pull.setPullMessage(msg);
        } else {
            pull = new PullSession(sess, msg);
            pullMap.putIfAbsent(sess.id(), pull);
        }
        this.dispatch();
    }

    void dispatch() throws IOException {
        MessagePacket msg = null;
        while (pullMap.size() > 0 && (msg = msgQ.poll()) != null) {
            this.lastUpdateTime = System.currentTimeMillis();

            Iterator<Map.Entry<String, PullSession>> iter = pullMap.entrySet().iterator();
            while (iter.hasNext()) {
                PullSession sess = iter.next().getValue();
                if (sess == null || !sess.getSession().isActive()) {
                    iter.remove();
                    continue;
                }
                sess.getMsgQ().offer(msg);
            }
        }

        Iterator<Map.Entry<String, PullSession>> iter = pullMap.entrySet().iterator();
        while (iter.hasNext()) {
            PullSession pull = iter.next().getValue();
            if (pull == null || !pull.getSession().isActive()) {
                iter.remove();
                continue;
            }
            try {
                pull.lock.lock();
                MessagePacket pullMessagePacket = pull.pullMessagePacket;
				if(pullMessagePacket == null) continue;
                msg = pull.getMsgQ().poll();
                if (msg == null) continue;
                pull.getSession().write(ResponseUtils.buildResponse(pull.getPullMessagePacket(),msg,"success","200",true));
                pull.pullMessagePacket = null;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                pull.lock.unlock();
            }
        }
    }

    @Override
    public void cleanSession(Session sess) {
        Iterator<Map.Entry<String, PullSession>> iter = pullMap.entrySet().iterator();
        while (iter.hasNext()) {
            PullSession pull = iter.next().getValue();
            if (sess == pull.session) {
                iter.remove();
                break;
            }
        }
    }

    @Override
    public void cleanSession() {
        Iterator<Map.Entry<String, PullSession>> iter = pullMap.entrySet().iterator();
        while (iter.hasNext()) {
            PullSession pull = iter.next().getValue();
            if (!pull.session.isActive()) {
                iter.remove();
            }
        }
    }

    @Override
    public String toString() {
        return "MQ [queue=" + queue + ", creator=" + getCreator() + "]";
    }

    @Override
    public void close() throws IOException {
     	PullSession pull = null;
		Iterator<PullSession> iter = pullMap.values().iterator();
		while( iter.hasNext()){
			try{
				pull = iter.next();
				pull.session.close();
			}catch(IOException e){
				log.warn(e.getMessage(), e);
			}
		}
    }

}
