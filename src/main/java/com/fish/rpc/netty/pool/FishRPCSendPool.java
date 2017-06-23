package com.fish.rpc.netty.pool;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.fish.rpc.util.FishRPCConfig;
//import com.fish.rpc.util.Log;
import com.fish.rpc.util.FishRPCLog;

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
	     config.setMinIdle(FishRPCConfig.getIntValue("fish.rpc.connect.min", 1));
	     config.setMaxWaitMillis(FishRPCConfig.getIntValue("fish.rpc.connect.timeout", 0)*1000);
	     
	     //对象最小的空闲时间。如果为小于等于0，最Long的最大值，如果大于0，当空闲的时间大于这个值时，执行移除这个对象操作。
	     //默认值是1000L * 60L * 30L;即30分钟。这个参数是强制性的，只要空闲时间超过这个值，就会移除
	     config.setMinEvictableIdleTimeMillis(-1);
	     //对象最小的空间时间，如果小于等于0，取Long的最大值，如果大于0，当对象的空闲时间超过这个值，并且当前空闲对象的数量大于最小空闲数量(minIdle)时，执行移除操作。
	     //这个和上面的minEvictableIdleTimeMillis的区别是，它会保留最小的空闲对象数量。而上面的不会，是强制性移除的。默认值是-1；
	     config.setSoftMinEvictableIdleTimeMillis(1000L * 60L * 30L);
	     config.setTestOnBorrow(false);
	     config.setTestOnReturn(false);
	     config.setTestWhileIdle(false);
	     //config.setNumTestsPerEvictionRun(-1);
		 fishRPCConnectionPool =  new GenericObjectPool<FishRPCConnection>(new FishRPCSendPoolFactory(),config);
	 }
	 
	 public FishRPCConnection borrow(){
		 FishRPCConnection connection = null;
		 try{ 
			 connection =  fishRPCConnectionPool.borrowObject();
			 FishRPCLog.debug("borrow an object named %s", connection.getName());
			 if(connection!=null && !connection.isValidate()){
				 connection.connect();
			 }
			 return connection;
		 }catch(final Exception e){
			 //throw new RuntimeException(e);
			 return connection;
		 }
	 }
	 
	 public void giveBack(final FishRPCConnection object){
		 if(object==null)return;
		 FishRPCLog.debug("return an object named %s", object.getName());
		 fishRPCConnectionPool.returnObject(object);
	 }
	 
	 public void reset(){
		 FishRPCLog.info("FishRPC client reset connect pool.");
		 instance = null;
	 }
	 
}
