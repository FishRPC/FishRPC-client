package com.fish.rpc.netty.pool;

import java.net.InetSocketAddress;

import com.fish.rpc.core.event.MessageSendEvent;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.send.RequestCallback;
import com.fish.rpc.netty.send.RequestChannelInit;
import com.fish.rpc.netty.send.RequestHandler;
import com.fish.rpc.util.FishRPCConfig;
//import com.fish.rpc.util.Log;
import com.fish.rpc.util.FishRPCLog;
import com.fish.rpc.util.TimeUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class FishRPCConnection {
 
	private static String server = FishRPCConfig.getStringValue("fish.rpc.server", "127.0.0.1:5050");
    private static InetSocketAddress remoteAddr = new InetSocketAddress(server.split(":")[0],Integer.parseInt(server.split(":")[1]));
	private String name;
	private Channel channel;  
	 
	
	public FishRPCConnection(String _name){
		this.name = _name;
	}
	
	public boolean connect() throws Exception{
		try {
			ChannelFuture channelFuture = FishRPCBootstrap.bootstrap
	                .remoteAddress(remoteAddr)
	        		.handler(new RequestChannelInit())
	        		.connect().sync(); 
			
	        channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (channelFuture.isSuccess()) {
						 FishRPCLog.debug("New FishRPC connection %s to %s:%s", name,remoteAddr.getAddress().getHostAddress(),remoteAddr.getPort());
						 channel = channelFuture.channel();
					 } 
				} 
	        });  
		} catch (Exception e) { 
			throw e;
		}finally{ 
		}
         
        return Boolean.TRUE;
	}
	
	public void destory(){
		if(channel!=null && channel.isActive()){
			channel.close();
		}
	}
	 
	public boolean isValidate(){
		return (channel!=null && channel.isActive());
	}
	
	public void setName(String name) {
		this.name = name;
	} 
	public String getName(){
		return name;
	}
	
	public void write(final FishRPCRequest request,RequestCallback callback){
		RequestHandler handler = channel.pipeline().get(RequestHandler.class);
		handler.sendRequest(request,callback); 
	}
	
	public void write(MessageSendEvent event){
		final FishRPCRequest request = event.getRequest();
		FishRPCLog.debug("The request %s ,client-send-start at %s",request.getRequestId(),TimeUtil.currentDateString());
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
         	FishRPCLog.debug("The request %s ,client-send-end at %s",request.getRequestId(),TimeUtil.currentDateString());
        }
        });
	}
	 
}
