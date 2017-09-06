package com.fish.rpc.netty.send;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.MessageReceiveEvent;
import com.fish.rpc.dto.FishRPCHeartbeat;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCLog;
//import com.fish.rpc.util.Log;
import com.fish.rpc.util.TimeUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class RequestHandler extends ChannelDuplexHandler {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx); 
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx); 
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if( msg instanceof FishRPCResponse ){
			FishRPCResponse response = (FishRPCResponse) msg;
			FishRPCLog.debug("[RequestHandler][channelRead][读取数据：%s][请求ID：%s]",TimeUtil.currentDateString(),response.getRequestId());
			MessageReceiveEvent event = new MessageReceiveEvent(response);
			AnsyEventBusCenter.getInstance().post(event);
			return;
		}
		super.channelRead(ctx, msg);
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
 }
