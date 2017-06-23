package com.fish.rpc.netty.send;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCConfig;

/**
 * 维护一个callBack
 * @author fish
 *
 */
public class RequestCallback {
	
	private FishRPCResponse response;
	private FishRPCRequest request;
	private Lock lock = new ReentrantLock();
	private Condition finish = lock.newCondition();
	
	public RequestCallback(FishRPCRequest request){
		this.request = request;
	}
	
	public FishRPCResponse getResponse() throws InterruptedException{
		try{
 			lock.lock();
			finish.await(FishRPCConfig.getIntValue("fish.rpc.client.read.timeout", 10), TimeUnit.SECONDS);
 			return response;
		}finally{
			lock.unlock();
		}
	}
	
	public void over(FishRPCResponse response){
		try{
			lock.lock();
			finish.signal();
 			this.response = response;
		}finally{
			lock.unlock();
		}
	}
	
	public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
