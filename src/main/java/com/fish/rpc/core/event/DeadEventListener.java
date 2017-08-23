package com.fish.rpc.core.event;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fish.rpc.util.FishRPCLog;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

public class DeadEventListener {
	@Subscribe
	@AllowConcurrentEvents
	public void listen(DeadEvent event){
		
		FishRPCLog.warn("[DeadEventListener][listen][未订阅的事件][%s]", ReflectionToStringBuilder.toString(event));
		
	}
}
