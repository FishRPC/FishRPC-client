package com.fish.rpc.netty.watch;

import com.fish.rpc.netty.connections.IConnection;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 连接检测狗
 * 
 * @author fish
 *
 */
@Sharable
public abstract class ConnectionWatch extends ChannelInboundHandlerAdapter implements  IChannelHolder {

  	private final IConnection connection;
  	
	public ConnectionWatch(IConnection connection) {
		this.connection = connection;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connection.connect();
		ctx.fireChannelInactive();
	} 
}
