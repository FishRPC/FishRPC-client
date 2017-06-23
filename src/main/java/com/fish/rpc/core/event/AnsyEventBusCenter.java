package com.fish.rpc.core.event;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.fish.rpc.parallel.FishRPCThreadPool;
import com.fish.rpc.util.FishRPCLog;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.MoreExecutors;

public class AnsyEventBusCenter {
	private Map<String, Class<? extends IEventListener>> registerListenerContainers = Maps.newConcurrentMap() ;
	
	private AsyncEventBus eventBus = new AsyncEventBus("FishRPC-Async-EventBus",
			MoreExecutors.listeningDecorator(
		    		(ThreadPoolExecutor) FishRPCThreadPool.getExecutor(
		    				Runtime.getRuntime().availableProcessors()+1,
		        			-1
		        		))
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
			FishRPCLog.error(e, e.getMessage()+" register listener erro, clazzName=%s", clazzName);
		}
	}
	
	public  void unRegister(IEventListener listener){
		eventBus.unregister(listener);
	}
	
	public  void post(Event event){
		eventBus.post(event);
	}
	
	
}
