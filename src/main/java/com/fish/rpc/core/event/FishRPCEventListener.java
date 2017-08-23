package com.fish.rpc.core.event;

import com.fish.rpc.dto.FishRPCResponse;
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
 		
		FishRPCLog.info("[FishRPCEventListener][action][接收事件][%s]",event);
		
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
		FishRPCLog.warn("[FishRPCEventListener][action][不支持事件][%s]",event);
 	}
	
	private void invokeSend(MessageSendEvent event){
		FishRPCConnection connection = null;
		try{ 
			EventMap.getInstance().put(event);
			connection = FishRPCSendPool.getInstance().borrow();
			if(connection==null || !connection.isValidate()){
				FishRPCLog.error("[FishRPCEventListener][invokeSend][连接无效：%s][立即返回默认值][事件：%s]",connection, event);
				Event sendEvent = EventMap.getInstance().get(event.getEventId());
				if(sendEvent instanceof MessageSendEvent){
					MessageSendEvent messageSendEvent = (MessageSendEvent)sendEvent;
					//返回一个默认的响应
					FishRPCResponse response = new FishRPCResponse();
					response.setRequestId(event.getRequest().getRequestId());
					response.setCode(-1);
					response.setError("RPC连接无效");
					response.setResult(new Exception("RPC连接无效"));
					messageSendEvent.over(response);
				}
				return ;
			} 
			connection.write((MessageSendEvent)event);
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
