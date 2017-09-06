package com.fish.rpc.core.event;

import java.util.Map;

import com.fish.rpc.parallel.FishRPCThreadPool;
import com.fish.rpc.util.FishRPCConfig;
import com.fish.rpc.util.FishRPCLog;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;

public class AnsyEventBusCenter {
	
	private Map<String, Class<? extends IEventListener>> registerListenerContainers = Maps.newConcurrentMap() ;
	
	private AsyncEventBus eventBus = new AsyncEventBus("FishRPC-Async-EventBus",
				
				FishRPCThreadPool.getExecutor(
    				"FishRPC-Async-EventBus",
    				FishRPCConfig.PARALLEL*2,
        			-1
        		)
				
			);  
	private static class AnsyEventBusCenterHolder {
		 private static final AnsyEventBusCenter instance = new AnsyEventBusCenter();
	}
	private AnsyEventBusCenter(){}
	
	public static AnsyEventBusCenter getInstance(){
		return AnsyEventBusCenterHolder.instance;
	}
	
	public  void register(Class<? extends IEventListener> clazz){
		String clazzName = clazz.getSimpleName() ;
		if(registerListenerContainers.containsKey(clazzName)) { 
			return ;
		}
		try{
			registerListenerContainers.put(clazzName, clazz);
			Object obj = registerListenerContainers.get(clazzName).newInstance();
			eventBus.register(obj);
		}catch(Exception e){
			FishRPCLog.error(e, "[AnsyEventBusCenter][register][Exception:%s]", e.getMessage());
		}
	}
	
	public  void unRegister(Class<? extends IEventListener> clazz){
		
		try {
			String clazzName = clazz.getSimpleName() ;
			Object obj = registerListenerContainers.get(clazzName).newInstance();
			if( obj == null ){
				return ;
			}
			eventBus.unregister(obj);
		} catch (InstantiationException | IllegalAccessException e) {
			FishRPCLog.error(e, "[AnsyEventBusCenter][unRegister][Exception:%s]", e.getMessage());
		} 
	}
	
	public  void post(Event event){
		eventBus.post(event);
	} 
}
