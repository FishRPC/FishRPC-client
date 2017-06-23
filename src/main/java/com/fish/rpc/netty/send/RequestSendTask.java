package com.fish.rpc.netty.send;

import java.util.concurrent.Callable;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;

public class RequestSendTask implements Callable<Boolean> {
    
	private FishRPCRequest request;
    private RequestCallback callback ;
    private FishRPCConnection connection;
    
    public RequestSendTask(FishRPCRequest request,RequestCallback callback,FishRPCConnection conn){
    	this.request = request;
    	this.callback = callback;
    	this.connection = conn;
    }
	@Override
	public Boolean call()  {
		try{
 			connection.write(request,callback); 
		}finally{
			FishRPCSendPool.getInstance().giveBack(connection);
		}
        return Boolean.TRUE;
    }

}
