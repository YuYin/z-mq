/*
 *
 * Copyright (c) 2005-2017 All Rights Reserved.
 */
package com.z.mq.client;

import java.io.IOException;

/**
 * @author <a href=mailto:someharder@gmail.com>yinyu</a> 2020/8/14
 */
public interface Client extends Invoker {

    void startHeartbeat(int heartbeatInSeconds);
    void stopHeartbeat();
	void heartbeat();

	boolean hasConnected();
	void connectAsync() throws IOException;
	void connectSync(long timeout) throws IOException, InterruptedException;
	void ensureConnectedAsync();
	void ensureConnected() throws IOException, InterruptedException;

	void onError(ErrorHandler errorHandler);
    void onConnected(ConnectedHandler connectedHandler);
    void onDisconnected(DisconnectedHandler disconnectedHandler);



	public static interface ConnectedHandler {
		void onConnected() throws IOException;
	}

	public static interface DisconnectedHandler {
		void onDisconnected() throws IOException;
	}

	public static interface ErrorHandler {
		void onError(Throwable e, Session session) throws IOException;
	}

	public static interface MsgHandler<T> {
		void handle(T msg, Session session) throws IOException;
	}
}
