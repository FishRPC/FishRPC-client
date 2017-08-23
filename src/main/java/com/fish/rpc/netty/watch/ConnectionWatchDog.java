package com.fish.rpc.netty.watch;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.fish.rpc.util.FishRPCLog;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
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

	private final Bootstrap bootstrap;
	private final Timer timer;
	private final InetSocketAddress remortAddr;
 

	private volatile boolean reconnect = true;
	private int attempts;

	public ConnectionWatchDog(Bootstrap bootstrap, Timer timer, InetSocketAddress remortAddr, boolean reconnect) {
		this.bootstrap = bootstrap;
		this.timer = timer;
		this.remortAddr = remortAddr; 
		this.reconnect = reconnect;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		attempts = 0;
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (reconnect) {
			if (attempts < 10) {
				attempts++;
				int timeout = 2 << attempts; // 重连的间隔时间会越来越长
				FishRPCLog.debug("[ConnectionWatchDog][channelInactive][%s秒后重连][第%s次重连]",timeout,attempts);
				timer.newTimeout(this, timeout, TimeUnit.SECONDS);
			}
		}
		ctx.fireChannelInactive();
	}

	public void run(Timeout timeout) throws Exception {
		ChannelFuture future; 
		synchronized (bootstrap) {
			bootstrap.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(handlers());
				}
			});
			future = bootstrap.connect(remortAddr);
		} 
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				boolean succeed = f.isSuccess();
 				if (!succeed) { 
					f.channel().pipeline().fireChannelInactive();
				} else {
					FishRPCLog.debug("[ConnectionWatchDog][run][第%s次重连][成功]",attempts);
				}
			}
		}); 
	}
}
