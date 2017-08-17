package com.fish.rpc.core.client;

import com.google.common.reflect.Reflection;

public class FishRPCExecutorClient {
	 
	 private static class FishRPCExecutorHolder {
		 private static final FishRPCExecutorClient instance = new FishRPCExecutorClient();
	 }

	 public static FishRPCExecutorClient getInstance() {
	     return FishRPCExecutorHolder.instance;
	 }
	 
	 private FishRPCExecutorClient(){
	 } 
	 public <T> T getBean(Class<T> rpcInterface) {
	     return (T) Reflection.newProxy(rpcInterface, new FishRPCProxy<T>());
	 } 
}
