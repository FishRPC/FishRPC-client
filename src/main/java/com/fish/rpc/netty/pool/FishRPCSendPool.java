package com.fish.rpc.netty.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.fish.rpc.util.FishRPCConfig;

public class FishRPCSendPool {
	
	private  GenericObjectPool<FishRPCConnection> fishRPCConnectionPool;
	private static volatile FishRPCSendPool instance; 
 
	public static FishRPCSendPool getInstance() {
		  if (instance == null) {
	            synchronized (FishRPCSendPool.class) {
	                if (instance == null) {
	                	instance = new FishRPCSendPool();
	                }
	            }
	     } 
		 return instance;
	 }
	 
	 private FishRPCSendPool(){
		 GenericObjectPoolConfig config = new GenericObjectPoolConfig();
	 
		 config.setMaxTotal(FishRPCConfig.getIntValue("fish.rpc.connect.max", 10));
		 config.setMaxIdle(FishRPCConfig.getIntValue("fish.rpc.connect.max", 10));
	     config.setMinIdle(FishRPCConfig.getIntValue("fish.rpc.connect.min", 0));
	     config.setMaxWaitMillis(FishRPCConfig.getIntValue("fish.rpc.connect.timeout", 0));
	     config.setTimeBetweenEvictionRunsMillis(FishRPCConfig.getLongValue("fish.rpc.connect.check.interval", -1L));
	     config.setMinEvictableIdleTimeMillis(1000L * 60L * 30L);
	     config.setTestOnBorrow(false);
	     config.setTestOnReturn(false);
	     config.setTestWhileIdle(true);
	     
		 fishRPCConnectionPool =  new GenericObjectPool<FishRPCConnection>(new FishRPCSendPoolFactory(),config);
	 }
	 
	 public FishRPCConnection borrow(){
		 try{
			 return fishRPCConnectionPool.borrowObject();
		 }catch(final Exception e){
			 e.printStackTrace();
			 return null;
		 }
	 }
	 
	 public void giveBack(final FishRPCConnection object){
		 fishRPCConnectionPool.returnObject(object);
	 }
	 
}
