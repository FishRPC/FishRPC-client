package com.fish.rpc.core.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCConfig;

public class MessageSendEvent extends Event {

	private Lock lock = new ReentrantLock();
	private Condition finish = lock.newCondition();
	
	private FishRPCRequest request; 
	private FishRPCResponse response; 
	
	public MessageSendEvent(FishRPCRequest request){
		super(FISH_RPC_MSG_SEND_EVT,request.getRequestId());
		this.request = request;
	}
	
	public FishRPCRequest getRequest() {
		return request;
	}
	public void setRequest(FishRPCRequest request) {
		this.request = request;
	}
	
	/**
	 * 阻塞直到有结果或者超时
	 * @return
	 * @throws InterruptedException
	 */
	public FishRPCResponse sync() throws InterruptedException{
		try{
			int timeout = FishRPCConfig.getIntValue("fish.rpc.client.read.timeout", 3);
 			lock.lock();
			finish.await(timeout, TimeUnit.SECONDS);
 			return response;
		}finally{
			lock.unlock();
		}
	} 
	/**
	 * 通知阻塞线程获取结果
	 * @param response
	 */
	public void over(FishRPCResponse response){
		try{
			lock.lock();
			this.response = response;
			finish.signal(); 
		}finally{
			lock.unlock();
		}
	} 
}
