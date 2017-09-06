package com.fish.rpc.netty.pool;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.fish.rpc.util.FishRPCLog;

public class FishRPCSendPoolFactory implements PooledObjectFactory<FishRPCConnection> {
	
	private AtomicInteger atomic = new AtomicInteger(0);  
	
	@Override
	public PooledObject<FishRPCConnection> makeObject() throws Exception {
		String name = "FishRPC-Connection-"+atomic.incrementAndGet();
		FishRPCConnection connection = new FishRPCConnection(name); 
		FishRPCLog.debug("[FishRPCSendPoolFactory][makeObject][Connection:%s]", name);
		return new DefaultPooledObject<FishRPCConnection>(connection);
	}
 
	@Override
	public void destroyObject(PooledObject<FishRPCConnection> pooledobject) throws Exception { 
		FishRPCLog.debug("[FishRPCSendPoolFactory][destroyObject][Connection:%s]", pooledobject.getObject().getName());
		pooledobject.getObject().destory();
	}
		 

	@Override
	public boolean validateObject(PooledObject<FishRPCConnection> pooledobject) { 
		FishRPCConnection connection = pooledobject.getObject();
		FishRPCLog.debug("[FishRPCSendPoolFactory][validateObject][Connection:%s]", connection.getName());
		return connection!=null && connection.isValidate();
	}

	@Override
	public void activateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		FishRPCLog.debug("[FishRPCSendPoolFactory][activateObject][Connection:%s]", pooledobject.getObject().getName());
		FishRPCConnection connection = pooledobject.getObject();
		connection.connect();
	}

	@Override
	public void passivateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		FishRPCLog.debug("[FishRPCSendPoolFactory][passivateObject][Connection:%s]", pooledobject.getObject().getName());
		/*FishRPCConnection connection = pooledobject.getObject();
		if(connection==null || !connection.isValidate()){
			connection.connect();
		} */
	} 

}
