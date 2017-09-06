package com.fish.rpc.netty.watch;

import java.util.concurrent.TimeUnit;

import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCServerNode;
import com.fish.rpc.netty.pool.FishRPCServerNodeManager;
import com.fish.rpc.util.FishRPCLog;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

/**
 * 连接检测狗
 * 
 * @author fish
 *
 */
@Sharable
public abstract class ConnectionWatchDog extends ChannelInboundHandlerAdapter implements TimerTask, IChannelHolder {

 	private final Timer timer;
 	private final FishRPCConnection connection;

	private volatile boolean reconnect = true;

	public ConnectionWatchDog(FishRPCConnection connection, Timer timer, boolean reconnect) {
 		this.timer = timer;
 		this.reconnect = reconnect;
		this.connection = connection;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		FishRPCServerNodeManager.getInstance().unUsable(connection.getNode());
		connection.connect(true);
		 if (reconnect) {
			FishRPCLog.debug("[ConnectionWatchDog][channelInactive][连接：%s][%s秒后重连]",connection.getName(),5);
			timer.newTimeout(this, 5, TimeUnit.SECONDS);
		}
		ctx.fireChannelInactive();
	}

	public void run(Timeout timeout) throws Exception {
		if( connection.isValidate() ){
			return ;
		}
		connection.connect();
	}

}
