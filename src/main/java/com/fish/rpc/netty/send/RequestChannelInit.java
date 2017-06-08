package com.fish.rpc.netty.send;

import com.fish.rpc.serialize.protostuff.ProtostuffDecoder;
import com.fish.rpc.serialize.protostuff.ProtostuffEncoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class RequestChannelInit extends  ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
	    pipeline.addLast(new ProtostuffEncoder());
	    pipeline.addLast(new ProtostuffDecoder(true));
	    pipeline.addLast(new RequestHandler());
	}

}
