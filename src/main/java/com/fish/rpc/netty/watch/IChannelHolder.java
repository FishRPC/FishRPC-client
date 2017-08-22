package com.fish.rpc.netty.watch;

import io.netty.channel.ChannelHandler;

public interface IChannelHolder {
	ChannelHandler[] handlers();
}
