package com.fish.rpc.core.event;

import com.fish.rpc.dto.FishRPCResponse;

public class MessageReceiveEvent extends Event {
	
	private FishRPCResponse response; 
	
	public MessageReceiveEvent(FishRPCResponse response){
		super(FISH_RPC_MSG_RECEIVE_EVT,response.getRequestId());
		this.response = response;
	}

	public FishRPCResponse getResponse() {
		return response;
	}

	public void setResponse(FishRPCResponse response) {
		this.response = response;
	}
	
	
}
