package com.fish.rpc.netty.send;

import java.util.concurrent.ConcurrentHashMap;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.TimeUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
		 System.out.println(response.getRequestId()+",client-read:"+TimeUtil.currentDateString());
	     String messageId = response.getRequestId();
	     RequestCallback callBack = mapCallBack.get(messageId);
	     if (callBack != null) {
	    	 mapCallBack.remove(messageId);
	         callBack.over(response);
	     }
	 }
	 @Override
	 public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	 	cause.printStackTrace();
	    ctx.close();
	 }

	 public void close() {
		 channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	 }
	 
	 
	 public void sendRequest(FishRPCRequest request,RequestCallback callback){
		 mapCallBack.put(request.getRequestId(), callback);
		 channel.writeAndFlush(request);
	 }
}
