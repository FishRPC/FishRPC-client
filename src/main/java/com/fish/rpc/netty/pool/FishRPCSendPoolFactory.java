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
		//System.out.println("create");
		return  connection; 
	}

	@Override
	public PooledObject<FishRPCConnection> wrap(FishRPCConnection connection) {
		//System.out.println("warp");
		return new DefaultPooledObject<FishRPCConnection>(connection);
	}
	 

	@Override
	public void destroyObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		//System.out.println("destroy");
		pooledobject.getObject().destory();
	}
		 

	@Override
	public boolean validateObject(PooledObject<FishRPCConnection> pooledobject) {
		//System.out.println("validate");
		FishRPCConnection connection = pooledobject.getObject();
		return connection.isValidate();
	}

	@Override
	public void activateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		 
		/*FishRPCConnection connection = pooledobject.getObject();
		if(connection==null || !connection.isValidate()){
			connection.connect();
		}*/
	}

	@Override
	public void passivateObject(PooledObject<FishRPCConnection> pooledobject) throws Exception {
		 
		/*FishRPCConnection connection = pooledobject.getObject();
		if(connection==null || !connection.isValidate()){
			connection.connect();
		} */
	} 

}
