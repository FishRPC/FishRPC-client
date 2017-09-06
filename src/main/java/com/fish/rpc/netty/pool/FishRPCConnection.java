package com.fish.rpc.netty.pool;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.fish.rpc.core.event.MessageSendEvent;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.send.RequestHandler;
import com.fish.rpc.netty.watch.ConnectionWatchDog;
import com.fish.rpc.serialize.kryo.KryoDecoder;
import com.fish.rpc.serialize.kryo.KryoEncoder;
//import com.fish.rpc.util.Log;
import com.fish.rpc.util.FishRPCLog;
import com.fish.rpc.util.TimeUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

public class FishRPCConnection {

	/*private static String server = FishRPCConfig.getStringValue("fish.rpc.server", "127.0.0.1:5050");
	private static InetSocketAddress remoteAddr = new InetSocketAddress(server.split(":")[0], Integer.parseInt(server.split(":")[1]));
	*/
	protected final HashedWheelTimer timer = new HashedWheelTimer();
	private String name;
	private Channel channel;
	private FishRPCServerNode node;

	public FishRPCConnection(String _name) {
		this.name = _name;
	}

	public boolean connect()  {
		return connect(false);
	}

	public boolean connect(final boolean isReconnect)   {
		ChannelFuture channelFuture = null;
		this.node = FishRPCServerNodeManager.getInstance().getRandNode();
		if(node == null){
			FishRPCLog.error("[FishRPCConnection][connect][无可用服务Node]");
			return false;
		}
		try {
			final InetSocketAddress remoteAddr = new InetSocketAddress(node.getIp(),node.getPort());
			final ConnectionWatchDog watchdog = new ConnectionWatchDog(this, timer,true) {
				public ChannelHandler[] handlers() {
					return new ChannelHandler[] { 
							this, 
							new IdleStateHandler(0, 50, 0, TimeUnit.SECONDS),
							new KryoEncoder(), 
							new KryoDecoder(), 
							new RequestHandler() 
						};
				}
			};
			synchronized (FishRPCBootstrap.bootstrap) {
				channelFuture = FishRPCBootstrap.bootstrap.handler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(watchdog.handlers());
					}
				}).connect(remoteAddr); 
				
				channelFuture.isDone();
				
				channelFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						if (channelFuture.isSuccess()) {
							if (isReconnect) {
								FishRPCLog.info("[FishRPCConnection][connect][重建RPC连接成功][%s][%s][%s]", name,
										remoteAddr.getAddress().getHostAddress(), remoteAddr.getPort());
							} else {
								FishRPCLog.info("[FishRPCConnection][connect][新建RPC连接成功][%s][%s][%s]", name,
										remoteAddr.getAddress().getHostAddress(), remoteAddr.getPort());
							}
							channel = channelFuture.channel();
							node.setUsable(true);
						}else{
							if (isReconnect) {
								FishRPCLog.error("[FishRPCConnection][connect][重建RPC连接失败][%s][%s][%s]", name,
										remoteAddr.getAddress().getHostAddress(), remoteAddr.getPort());
							} else {
								FishRPCLog.error("[FishRPCConnection][connect][新建RPC连接失败][%s][%s][%s]", name,
										remoteAddr.getAddress().getHostAddress(), remoteAddr.getPort());
							}
							node.setUsable(false);
							channelFuture.channel().pipeline().fireChannelInactive();
						}
					}
				});
			}

		} catch (Exception e) {
			node.setUsable(false);
			FishRPCLog.error(e,"[FishRPCConnection][connect][建立RPC连接失败][%s][%s]", name,node); 
			return false;
		} finally {

		} 
		return true;
	}

	public void destory() {
		if (channel != null && channel.isActive()) {
			channel.close();
		}
	}

	public boolean isValidate() {
		return (channel != null && channel.isActive());
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void write(MessageSendEvent event) {
		final FishRPCRequest request = event.getRequest();
		FishRPCLog.debug("[FishRPCConnection][write][开始发送数据:%s][请求ID：%s]", TimeUtil.currentDateString(),
				request.getRequestId());
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				FishRPCLog.debug("[FishRPCConnection][write][发送数据结束:%s][请求ID：%s]", TimeUtil.currentDateString(),
						request.getRequestId());
			}
		});
	}
	
	public FishRPCServerNode getNode(){
		return node;
	}

}
