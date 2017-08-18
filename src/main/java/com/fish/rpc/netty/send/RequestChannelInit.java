package com.fish.rpc.netty.send;

import com.fish.rpc.serialize.kryo.KryoDecoder;
import com.fish.rpc.serialize.kryo.KryoEncoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class RequestChannelInit extends  ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new KryoEncoder());
        pipeline.addLast(new KryoDecoder());/*
        pipeline.addLast(new ProtostuffEncoder());
	    pipeline.addLast(new ProtostuffDecoder(true));*/
	    pipeline.addLast(new RequestHandler());
	}

}
