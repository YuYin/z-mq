package com.z.mq.client;

import com.z.mq.common.protocol.ResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends SimpleChannelInboundHandler<ResponsePacket> {

    private final static AttributeKey<String> sessionKey = AttributeKey.valueOf("session");
    private Map<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private ConcurrentHashMap<String, InvokeFuture> pendingRPC = new ConcurrentHashMap<>();

    public ClientHandler(DefaultClient client) {
        this.client = client;
    }

    private DefaultClient client;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session sess = attachSession(ctx);
        client.onSessionCreated(sess);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session sess = getSession(ctx);
        client.onSessionToDestroy(sess);
        sessionMap.remove(sess.id());
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ResponsePacket response) throws Exception {
        String requestId = response.getRequestId();
        InvokeFuture invokeFuture = PendingFutureManager.pendingRPC.get(requestId);
        if (invokeFuture != null) {
            pendingRPC.remove(requestId);
            invokeFuture.done(response);
        }
        	Session sess = getSession(ctx);
		client.onSessionMessage(response, sess);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception", cause);
        Session sess = getSession(ctx);
        client.onSessionError(cause, sess);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Session sess = getSession(ctx);
            client.onSessionIdle(sess);
        }
    }

    private Session attachSession(ChannelHandlerContext ctx) {
        Session sess = new TcpSession(ctx);
        Attribute<String> attr = ctx.attr(sessionKey);
        attr.set(sess.id());
        sessionMap.put(sess.id(), sess);
        return sess;
    }

    private Session getSession(ChannelHandlerContext ctx) {
        Attribute<String> attr = ctx.attr(sessionKey);
        if (attr.get() == null) {
            throw new IllegalThreadStateException("Missing sessionKey");
        }
        Session sess = sessionMap.get(attr.get());
        if (sess == null) {
            throw new IllegalThreadStateException("Session and ChannelHandlerContext mapping not found");
        }
        return sess;
    }

}
