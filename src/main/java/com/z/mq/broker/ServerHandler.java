package com.z.mq.broker;

import com.z.mq.client.Session;
import com.z.mq.client.TcpSession;
import com.z.mq.common.io.IoProcessor;
import com.z.mq.common.protocol.MessagePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC Handler RPC request processor
 */
public class ServerHandler  extends SimpleChannelInboundHandler<MessagePacket> {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);



    private final static AttributeKey<String> sessionKey = AttributeKey.valueOf("session");
	private Map<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

     private IoProcessor ioProcessor;

    public ServerHandler(IoProcessor ioProcessor) {
        this.ioProcessor = ioProcessor;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final MessagePacket request) throws Exception {
    	Session sess = getSession(ctx);
		ioProcessor.onSessionMessage(request, sess);
    }


	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Session sess = getSession(ctx);
		ioProcessor.onSessionError(cause, sess);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Session sess = attachSession(ctx);
		ioProcessor.onSessionCreated(sess);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Session sess = getSession(ctx);
		ioProcessor.onSessionToDestroy(sess);
		sessionMap.remove(sess.id());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			Session sess = getSession(ctx);
			ioProcessor.onSessionIdle(sess);
        }
	}

	private Session attachSession(ChannelHandlerContext ctx){
		Session sess = new TcpSession(ctx);
		Attribute<String> attr = ctx.attr(sessionKey);
		attr.set(sess.id());
		sessionMap.put(sess.id(), sess);
		return sess;
	}

	private Session getSession(ChannelHandlerContext ctx){
		Attribute<String> attr = ctx.attr(sessionKey);
		if(attr.get() == null){
			throw new IllegalThreadStateException("Missing sessionKey");
		}
		Session sess = sessionMap.get(attr.get());
		if(sess == null){
			throw new IllegalThreadStateException("Session and ChannelHandlerContext mapping not found");
		}
		return sess;
	}
}
