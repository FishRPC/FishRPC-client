package com.fish.rpc.netty.pool;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 维护服务器节点
 * @author fish
 *
 */
public class FishRPCServerNode {
	private int avgeResponseMillsTime;
	private String ip;
	private int port;
	private boolean usable;
	
	public int getAvgeResponseMillsTime() {
		return avgeResponseMillsTime;
	}
	public void setAvgeResponseMillsTime(int avgeResponseMillsTime) {
		this.avgeResponseMillsTime = avgeResponseMillsTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isUsable() {
		return usable;
	}
	public void setUsable(boolean usable) {
		this.usable = usable;
	}
	public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
	
}
