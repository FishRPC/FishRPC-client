package com.fish.rpc.netty.send;

import java.util.concurrent.ConcurrentHashMap;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.MessageReceiveEvent;
import com.fish.rpc.dto.FishRPCHeartbeat;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCLog;
//import com.fish.rpc.util.Log;
import com.fish.rpc.util.TimeUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class RequestHandler extends ChannelInboundHandlerAdapter {

	private ConcurrentHashMap<String, RequestCallback> mapCallBack = new ConcurrentHashMap<String, RequestCallback>();
	private volatile Channel channel;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.channel = ctx.channel();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FishRPCResponse response = (FishRPCResponse) msg;
 		
		FishRPCLog.debug("[RequestHandler][channelRead][读取数据：%s][请求ID：%s]",TimeUtil.currentDateString(),response.getRequestId());
		
		MessageReceiveEvent event = new MessageReceiveEvent(response);
		AnsyEventBusCenter.getInstance().post(event);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		FishRPCLog.error(cause,"[RequestHandler][exceptionCaught][Exception:%s]", cause.getMessage());
		ctx.close();
	}

	private static final FishRPCHeartbeat aFishRPCHeartbeat = new FishRPCHeartbeat();
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.WRITER_IDLE){
				FishRPCLog.debug("[RequestHandler][userEventTriggered][心跳][%s]",aFishRPCHeartbeat);
			 	ctx.writeAndFlush(aFishRPCHeartbeat);  
			}else {
				super.userEventTriggered(ctx, evt);
			}
		}
	}

	public void close() {
		channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	} 
 }
