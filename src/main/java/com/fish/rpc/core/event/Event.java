package com.fish.rpc.core.event;

public class Event {

	public static final String FISH_RPC_MSG_SEND_EVT = "FISH_RPC_MSG_SEND_EVT";
	public static final String FISH_RPC_MSG_RECEIVE_EVT = "FISH_RPC_MSG_RECEIVE_EVT";
	
	private String eventId;
	private String eventName;
	
	public Event(String name,String eventId){
		this.eventName = name;
		this.eventId = eventId;
	}
	
	@Override
    public String toString() {
        return "Event[eventName=" + eventName + "][eventId=" + eventId+"]";
    }
	
	public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	
}
