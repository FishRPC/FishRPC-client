package com.fish.rpc.netty.pool;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class FishRPCSendPoolFactory extends BasePooledObjectFactory<FishRPCConnection> {
	
	private AtomicInteger atomic = new AtomicInteger(0);  
	@Override
	public FishRPCConnection create() throws Exception {
		String name = "FishRPC-Connection-"+atomic.incrementAndGet();
		FishRPCConnection connection = new FishRPCConnection(name);
		connection.connect(); 
		return  connection; 
	}

	@Override
	public PooledObject<FishRPCConnection> wrap(FishRPCConnection connection) {
		return new DefaultPooledObject<FishRPCConnection>(connection);
	}
	 

	@Override
	public void destroyObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		pooledobject.getObject().destory();
		pooledobject = null;
	}

	@Override
	public boolean validateObject(PooledObject<FishRPCConnection> pooledobject) {
		return pooledobject.getObject().isValidate();
	}

	@Override
	public void activateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		//pooledobject.getObject().connect();
	}

	@Override
	public void passivateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		 
	} 

}
