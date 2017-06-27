package com.fish.rpc.core.event;

import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.FishRPCLog;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class FishRPCEventListener implements IEventListener{

	@Override 
	@Subscribe
	@AllowConcurrentEvents
	public void action(Event event) { 
		FishRPCLog.debug(String.format("[FishRPCEventListener ] action event=%s", event.toString()));
		if(event==null || event.getEventId()==null){
			return;
		}
		if(event instanceof MessageSendEvent){
			invokeSend((MessageSendEvent)event);
			return ;
		}else if(event instanceof MessageReceiveEvent){
			invokeReceive((MessageReceiveEvent)event);
			return ;
		}
		FishRPCLog.warn("FishRPCEventListener can not deal event which not support.The event is "+event.toString());
	}
	
	private void invokeSend(MessageSendEvent event){
		FishRPCConnection connection = null;
		try{ 
			connection = FishRPCSendPool.getInstance().borrow();
			if(connection==null || !connection.isValidate()){
				FishRPCLog.error(connection+"，FishRPCProxy happened RuntimeException,return default vale.Connection is not validate on FishRPCEventListener event = "+event.toString());
				return ;
			} 
			connection.write((MessageSendEvent)event);
			EventMap.getInstance().put(event);
		}finally{ 
			FishRPCSendPool.getInstance().giveBack(connection);
		}
		
	}

	private void invokeReceive(MessageReceiveEvent event){
		try{ 
			Event sendEvent = EventMap.getInstance().get(event.getEventId());
			if(sendEvent==null){
				return ;
			}
			if(sendEvent instanceof MessageSendEvent){
				MessageSendEvent messageSendEvent = (MessageSendEvent)sendEvent;
				messageSendEvent.over(event.getResponse());
			}
		}finally{
			EventMap.getInstance().remove(event.getEventId());
		}
		
	}

}
