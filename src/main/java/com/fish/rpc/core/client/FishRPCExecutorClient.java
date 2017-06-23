package com.fish.rpc.core.client;

import java.util.ArrayList;
import java.util.List;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.FishRPCEventListener;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.FishRPCConfig;
import com.google.common.reflect.Reflection;

public class FishRPCExecutorClient {
	
	private static void initRPCConnection(){
		long start = System.currentTimeMillis();
		System.out.println("Init fishRPC connection...");
		int minConnection = FishRPCConfig.getIntValue("fish.rpc.connect.min", 100);
		System.out.println("Init fishRPC connection,min="+minConnection);
		List<FishRPCConnection> initConns = new ArrayList<FishRPCConnection>();
		for(int i=0;i<minConnection;i++){
			FishRPCConnection connection = FishRPCSendPool.getInstance().borrow();
			initConns.add(connection);
		}
		for(FishRPCConnection conn:initConns){
			FishRPCSendPool.getInstance().giveBack(conn);
		}
		System.out.println("Init fishRPC connection done ,"+(System.currentTimeMillis() - start)+" ms");
	}
		
	 private static class FishRPCExecutorHolder {
		 private static final FishRPCExecutorClient instance = new FishRPCExecutorClient();
	 }

	 public static FishRPCExecutorClient getInstance() {
	     return FishRPCExecutorHolder.instance;
	 }
	 
	 private FishRPCExecutorClient(){
		 try {
				FishRPCConfig.initClient();
				initRPCConnection();
				//注册事件监听器
				AnsyEventBusCenter.getInstance().register(FishRPCEventListener.class); 
			} catch (Exception e) { 
				e.printStackTrace();
			}
	 } 
	 public <T> T getBean(Class<T> rpcInterface) {
	     return (T) Reflection.newProxy(rpcInterface, new FishRPCProxy<T>());
	 } 
}
