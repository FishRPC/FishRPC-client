package com.fish.rpc.core.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCConfig;
import com.fish.rpc.util.TimeUtil;

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
			int timeout = FishRPCConfig.getIntValue("fish.rpc.client.read.timeout", 10);
			lock.lock();
			request.setClientAwaitAtTime(System.currentTimeMillis());
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
			request.setClientSignalAtTime(System.currentTimeMillis());
			lock.unlock();
		}
	} 
	
	public String elapsedInfo(){
		if( response == null ){
			return "";
		}
		String template = "\n请求ID[%s]耗时分析>>>>>>>>>>>\n\t"
				+ "客户端请求构造：%s\n\t"
				+ "客户端请求发送开始：%s\n\t"
				+ "客户端请求发送结束：%s\n\t"
				+ "服务端接收：%s\n\t"
				+ "服务端业务逻辑开始：%s\n\t"
				+ "服务端业务逻辑结束：%s\n\t"
				+ "服务端响应发送开始：%s\n\t"
				+ "客户端阻塞：%s\n\t"
				+ "客户端唤醒：%s\n"
				+ "节点耗时分析>>>>>>>>>>>\n\t"
				+ "客户端到服务端网络耗时：%s ms\n\t"
				+ "服务端到客户端网络耗时：%s ms\n\t"
				+ "服务端处理业务逻辑耗时：%s ms\n\t"
				+ "阻塞直到唤醒耗时：%s ms\n";
		return String.format(template,
				request.getRequestId(),
				TimeUtil.formatMillsecond(request.getConstructionTime()),
				TimeUtil.formatMillsecond(request.getClientStartSendDataTime()),
				TimeUtil.formatMillsecond(request.getClientDoneSendDataTime()),
				TimeUtil.formatMillsecond(response.getServerReceiveAtTime()),
				TimeUtil.formatMillsecond(response.getServerStartBusinessTime()),
				TimeUtil.formatMillsecond(response.getServerDoneBusinessTime()),
				TimeUtil.formatMillsecond(response.getServerDoneSendDataTime()),
				TimeUtil.formatMillsecond(request.getClientAwaitAtTime()),
				TimeUtil.formatMillsecond(request.getClientSignalAtTime()),
				(response.getServerReceiveAtTime() - request.getClientDoneSendDataTime()),
				(request.getClientSignalAtTime() - response.getServerDoneSendDataTime()),
				(response.getServerDoneBusinessTime() - response.getServerStartBusinessTime()),
				(request.getClientSignalAtTime() - request.getClientAwaitAtTime())
			);
	}
}
