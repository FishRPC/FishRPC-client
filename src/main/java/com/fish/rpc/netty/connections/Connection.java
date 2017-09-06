package com.fish.rpc.netty.connections;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fish.rpc.core.event.MessageSendEvent;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.pool.FishRPCBootstrap;
import com.fish.rpc.netty.send.RequestHandler;
import com.fish.rpc.netty.watch.ConnectionWatch;
import com.fish.rpc.serialize.kryo.KryoDecoder;
import com.fish.rpc.serialize.kryo.KryoEncoder;
import com.fish.rpc.util.FishRPCLog;
import com.fish.rpc.util.TimeUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class Connection implements IConnection,TimerTask{

	private final HashedWheelTimer timer = new HashedWheelTimer();

	private String name;
	private String ip;
	private int port;
	
	private Channel channel; 
	private AtomicInteger reTryTimes = new AtomicInteger(0);  

	public Connection(String ip,int port){
		this.ip = ip;
		this.port = port;
		this.name = "connection->"+ip+":"+port;
	}
	
	@Override
	public void connect() {
		ChannelFuture channelFuture = null;
		try {
			final InetSocketAddress remoteAddr = new InetSocketAddress(ip,port);
			final ConnectionWatch watch = new ConnectionWatch(this) {
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
						ch.pipeline().addLast(watch.handlers());
					}
				}).connect(remoteAddr);  
				
				channelFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						if (channelFuture.isSuccess()) {
							FishRPCLog.info("[Connection][connect][RPC连接成功][%s]", name);
							ConnectionManager.getInstance().transfer2useable(Connection.this);
							Connection.this.reTryTimes.set(0);
							channel = channelFuture.channel();
						}else{
							FishRPCLog.error("[Connection][connect][RPC连接失败][%s]", name);
							ConnectionManager.getInstance().transfer2invalid(Connection.this);
							channel = null;
							Connection.this.check(); 
						}
					}
				});
			}
		} catch (Exception e) { 
			FishRPCLog.error(e,"[Connection][connect][建立RPC连接失败][%s][%s]", name);  
			ConnectionManager.getInstance().transfer2invalid(Connection.this);
			channel = null; 
			Connection.this.check();
		} finally {
		}
	}

	@Override
	public void write(MessageSendEvent event) {
		final FishRPCRequest request = event.getRequest();
		request.setClientStartSendDataTime(System.currentTimeMillis());
		FishRPCLog.debug("[Connection][%s][write][开始发送数据:%s][请求ID：%s]", name,TimeUtil.currentDateString(),
				request.getRequestId());
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				FishRPCLog.debug("[Connection][%s][write][发送数据结束:%s][请求ID：%s]",name, TimeUtil.currentDateString(),
						request.getRequestId());
				request.setClientDoneSendDataTime(System.currentTimeMillis());
			}
		});
	}

	@Override
	public void close() {
		if( channel != null ){
			channel.close();
		}
	}

	@Override
	public boolean useable() {
		return ( channel !=null && channel.isActive() );
	} 

	@Override
	public void run(Timeout timeout) throws Exception {
		connect();
	}

	@Override
	public void check() {
		int times = reTryTimes.incrementAndGet();
		int delay = 2<<times;
		FishRPCLog.info("[Connection][connect][%s秒后将尝试第%s次重连][%s]",delay,times, name);
		timer.newTimeout(this,delay , TimeUnit.SECONDS);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
