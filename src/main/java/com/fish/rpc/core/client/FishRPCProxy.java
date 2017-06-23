package com.fish.rpc.core.client;

import java.lang.reflect.Method;
import java.util.UUID;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.MessageSendEvent;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.FishRPCLog;
import com.google.common.reflect.AbstractInvocationHandler;

public class FishRPCProxy<T>  extends AbstractInvocationHandler{
	 
   /* private static ListeningExecutorService  threadPoolExecutor =  MoreExecutors.listeningDecorator(
    		(ThreadPoolExecutor) FishRPCThreadPool.getExecutor(
    			FishRPCConfig.getIntValue("fish.rpc.system.thread.num", 10),
    			-1
    		));*/

	@Override
	public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
       
	    FishRPCRequest request = new FishRPCRequest();
	    request.setRequestId(UUID.randomUUID().toString());
	    request.setClassName(method.getDeclaringClass().getName());
	    request.setMethodName(method.getName());	
	    request.setParamsType(method.getParameterTypes());
	    request.setParamsVal(args);
     
	    
	    MessageSendEvent event =  new MessageSendEvent(request); 
	    AnsyEventBusCenter.getInstance().post(event);
	    
	    /*RequestCallback callback = new RequestCallback(request);
	    RequestSendTask task = new RequestSendTask(request,callback,connection);
	    ListenableFuture<Boolean> result =  threadPoolExecutor.submit(task);*/
       
	    FishRPCResponse response = event.sync();
	    
	    if(response!=null && response.getCode()!=-1 && response.getResult()!=null){
    	   return response.getResult();
	    }
	    
   	   	FishRPCLog.error("FishRPCProxy::RuntimeException,connection is not validate,may be server was down.");
   	   	return  returnDefault(method.getReturnType());
    }
	
	private Object returnDefault(Class<?> returnType){
		  if(!returnType.isPrimitive()){
	    	   return null;
	       }
	       if(returnType == int.class || returnType == short.class 
	    		   || returnType == long.class || returnType==byte.class ){
	    	   return 0;
	       }
	       if(returnType == double.class || returnType == float.class  ){
	    	   return 0.0;
	       }
	       if(returnType == char.class){
	    	   return "";
	       }
	       return null;
	}
}
