package com.fish.rpc.core.client;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.netty.send.RequestCallback;
import com.fish.rpc.netty.send.RequestSendTask;
import com.fish.rpc.parallel.FishRPCThreadPool;
import com.fish.rpc.util.FishRPCConfig;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class FishRPCProxy<T>  extends AbstractInvocationHandler{
	 
    private static ListeningExecutorService 
    threadPoolExecutor =  MoreExecutors.listeningDecorator(
    	 (ThreadPoolExecutor) FishRPCThreadPool
    	 	.getExecutor(FishRPCConfig.getIntValue("fish.rpc.system.thread.num", 10),-1) );

	@Override
	public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
       FishRPCRequest request = new FishRPCRequest();
       request.setRequestId(UUID.randomUUID().toString());
       request.setClassName(method.getDeclaringClass().getName());
       request.setMethodName(method.getName());	
       request.setParamsType(method.getParameterTypes());
       request.setParamsVal(args);
       
      
       RequestCallback callback = new RequestCallback(request);
       RequestSendTask task = new RequestSendTask(request,callback);
       threadPoolExecutor.submit(task);
   	   FishRPCResponse response = callback.getResponse();
       if(response!=null){
    	   return response.getResult();
       } 
       //异常情况返回类型默认值
       Class<?> returnType = method.getReturnType();
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
