package com.fish.rpc.netty.send;

import com.fish.rpc.serialize.kryo.KryoDecoder;
import com.fish.rpc.serialize.kryo.KryoEncoder;
import com.fish.rpc.util.FishRPCConfig;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class RequestChannelInit extends  ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new KryoEncoder());
        pipeline.addLast(new KryoDecoder());/*
        pipeline.addLast(new ProtostuffEncoder());
	    pipeline.addLast(new ProtostuffDecoder(true));*/
        pipeline.addLast("idleStateHandler", new IdleStateHandler(10, 5, 0));
	    pipeline.addLast(new RequestHandler());
	    
	    if(FishRPCConfig.getBooleanValue("fish.rpc.debug.mode", false)){
        	pipeline.addLast(new LoggingHandler());
        }
	}

}
