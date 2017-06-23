package com.fish.rpc.core.event;

import java.util.Map;

import com.google.common.collect.Maps;

public class EventMap {
	
	public static final Map<String,Event> maps = Maps.newConcurrentMap();

	private static class EventMapHolder {
		 private static final EventMap instance = new EventMap();
	}
	
	public static EventMap getInstance(){
		return EventMapHolder.instance;
	}
	
	public void put(Event event){
		maps.put(event.getEventId(), event);
	}
	
	public Event get(String eventId){
		return maps.get(eventId);
	}
	
	public void remove(String requestId){
		maps.remove(requestId);
	}

	public boolean containsKey(String requestId){
		return maps.containsKey(requestId);
	}
	
}
