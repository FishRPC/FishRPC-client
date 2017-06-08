package com.fish.rpc.netty.pool;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.send.RequestCallback;
import com.fish.rpc.netty.send.RequestChannelInit;
import com.fish.rpc.netty.send.RequestHandler;
import com.fish.rpc.util.FishRPCConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class FishRPCConnection {

    private static String server = FishRPCConfig.getStringValue("fish.rpc.server", "127.0.0.1:5050");
    private static InetSocketAddress remoteAddr = new InetSocketAddress(server.split(":")[0],Integer.parseInt(server.split(":")[1]));
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(FishRPCConfig.PARALLEL);

	private String name;
	private Channel channel; 
	
	public FishRPCConnection(String name){
		this.name = name;
	}
	
	public void connect(){
		Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(remoteAddr)
        		.handler(new RequestChannelInit());
        ChannelFuture channelFuture = b.connect();
        final CountDownLatch latch = new CountDownLatch(1);
        channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				 if (channelFuture.isSuccess()) { 
                     System.out.println("New FishRPC connection ["+name+"] to : " + remoteAddr.getAddress().getHostAddress() + ':' + remoteAddr.getPort());
                     channel = channelFuture.channel();
                     latch.countDown();
				 } else {
                     channel = channelFuture.channel();
	                 eventLoopGroup.schedule(new Runnable() {
	                	 @Override
	                     public void run() {
	                		 System.err.println("FishRPC server is down,start to reconnecting to: " + remoteAddr.getAddress().getHostAddress() + ':' + remoteAddr.getPort());
	                         try {
	                        	 connect();
	                         } catch (Exception e) {
	                        	 
	                         }
	                      }
	                    }, FishRPCConfig.getIntValue("fish.rpc.client.reconnection.delay", 5), TimeUnit.SECONDS);
	                } 
			}
        });
        try {
			latch.await();
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
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
	public void write(FishRPCRequest request,RequestCallback callback){
		RequestHandler handler = channel.pipeline().get(RequestHandler.class);
     	handler.sendRequest(request,callback);
	}
	 
}
