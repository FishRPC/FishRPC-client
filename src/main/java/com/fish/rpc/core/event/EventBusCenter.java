package com.fish.rpc.core.event;

import com.google.common.eventbus.EventBus;

public class EventBusCenter {

	private EventBus eventBus = new EventBus("FishRPC-Sync-EventBus");
	
	private static class EventBusCenterHolder {
		 private static final EventBusCenter instance = new EventBusCenter();
	}
	private EventBusCenter(){}
	
	public static EventBusCenter getInstance(){
		return EventBusCenterHolder.instance;
	}
	
	public  void register(Object obj){ 
		eventBus.register(obj);
	}
	
	public  void unRegister(Object obj){
		eventBus.unregister(obj);
	}
	
	public  void post(Object event){
		eventBus.post(event);
	}
}
