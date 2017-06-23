package com.fish.rpc.netty.send;

import java.util.concurrent.ConcurrentHashMap;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.MessageReceiveEvent;
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
	 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 FishRPCResponse response = (FishRPCResponse) msg;
		 FishRPCLog.debug("The request %s ,client-read at %s",response.getRequestId(),TimeUtil.currentDateString());
		 /*String messageId = response.getRequestId();
	     RequestCallback callBack = mapCallBack.get(messageId);
	     if (callBack != null) {
	    	 mapCallBack.remove(messageId);
	         callBack.over(response);
	     }*/
		 
		 MessageReceiveEvent event =  new MessageReceiveEvent(response);
		 AnsyEventBusCenter.getInstance().post(event);
	 }

	 @Override
	 public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		 FishRPCLog.error(cause, cause.getMessage(), "");
	     ctx.close();  
	 }

	 public void close() {
		 channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	 }
	 
	 public void sendRequest(final FishRPCRequest request,final RequestCallback callback){
		 FishRPCLog.debug("The request %s ,client-send-start at %s",request.getRequestId(),TimeUtil.currentDateString());
		 channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
             public void operationComplete(ChannelFuture channelFuture) throws Exception {
            	 mapCallBack.put(request.getRequestId(), callback);
        		 FishRPCLog.debug("The request %s ,client-send-end at %s",request.getRequestId(),TimeUtil.currentDateString());
             }
         });
	 }
}
