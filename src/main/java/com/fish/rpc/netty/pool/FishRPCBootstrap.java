package com.fish.rpc.netty.pool;

import com.fish.rpc.util.FishRPCConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class FishRPCBootstrap {
    public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(FishRPCConfig.PARALLEL);
    
    public  static final Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);

}
