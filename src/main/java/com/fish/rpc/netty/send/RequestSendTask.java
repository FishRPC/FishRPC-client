package com.fish.rpc.netty.send;

import java.util.concurrent.Callable;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.TimeUtil;

public class RequestSendTask implements Callable<Boolean> {
      private FishRPCRequest request;
    private RequestCallback callback ;
    
    public RequestSendTask(FishRPCRequest request,RequestCallback callback){
    	this.request = request;
    	this.callback = callback;
    }
	@Override
	public Boolean call()  {
		FishRPCConnection connection = null;
		try{
			System.out.println(request.getRequestId()+",clent-borrow-start:"+TimeUtil.currentDateString());
			connection = FishRPCSendPool.getInstance().borrow();
			System.out.println(request.getRequestId()+",clent-borrow-end:"+TimeUtil.currentDateString());
			System.out.println(request.getRequestId()+",clent-send-start:"+TimeUtil.currentDateString());
			connection.write(request,callback);
			System.out.println(request.getRequestId()+",clent-send-end:"+TimeUtil.currentDateString());

		}finally{
			FishRPCSendPool.getInstance().giveBack(connection);
		}
        return Boolean.TRUE;
    }

}
