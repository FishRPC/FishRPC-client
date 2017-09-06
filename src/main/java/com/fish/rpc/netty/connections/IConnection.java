package com.fish.rpc.netty.connections;

import com.fish.rpc.core.event.MessageSendEvent;

public interface IConnection {

	public void connect();
	public boolean useable();
	public void write(MessageSendEvent event);
	public void close(); 
	public void check();
	public String getName();
	
}
