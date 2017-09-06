package com.fish.rpc.netty.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fish.rpc.util.FishRPCConfig;

public class FishRPCServerNodeManager {
	
	private Map<String,FishRPCServerNode> nodes = java.util.Collections.synchronizedMap(new HashMap<String,FishRPCServerNode>());
	
	private static class FishRPCServerNodeManagerHolder{
		private static final FishRPCServerNodeManager instance = new FishRPCServerNodeManager();
	}
	
	private FishRPCServerNodeManager(){
		String server = FishRPCConfig.getStringValue("fish.rpc.server", "127.0.0.1:5050");
		String[] servers = server.split(",");
		
		for(String s : servers){
			if(StringUtils.isEmpty(s)) continue;
			String[] ssplit = s.split(":");
			if(ssplit.length!=2) continue;
 			FishRPCServerNode node = new FishRPCServerNode();
			node.setIp(ssplit[0].trim());
			node.setPort(Integer.parseInt(ssplit[1].trim()));
			nodes.put(node.getIp()+":"+node.getPort(), node);
		}
	}
	
	public static FishRPCServerNodeManager getInstance(){
		return FishRPCServerNodeManagerHolder.instance;
	}
	
	public void unUsable(FishRPCServerNode node){
		String key = node.getIp()+":"+node.getPort();
		if( nodes.containsKey(key) ){
			nodes.get(key).setUsable(false);
		}
	} 
	/**
	 * 获取随机Node
	 * @return
	 */
	public FishRPCServerNode getRandNode(){
		Collection<FishRPCServerNode> nodeCollection = nodes.values();
		if( nodeCollection.size() == 0 ){
			return null;
		}
		
		List<FishRPCServerNode> list = new ArrayList<FishRPCServerNode>();
		list.addAll(nodeCollection);
		
		List<FishRPCServerNode> listUsable = new ArrayList<FishRPCServerNode>();
		for(FishRPCServerNode node : list){
			if( !node.isUsable() ){
				continue;
			}
			listUsable.add(node);
		}
		if( listUsable.size() == 0 ){
			return null;
		}
		
		java.util.Random random=new java.util.Random();
		int seed = listUsable.size();
		int index = random.nextInt(seed);
		
		return list.get(index); 
	}
	/**
	 * 获取最优Node,待实现
	 * @return
	 */
	public FishRPCServerNode getOptimalNode(){
		Collection<FishRPCServerNode> nodeCollection = nodes.values();
		if( nodeCollection.size() == 0 ){
			return null;
		}
		List<FishRPCServerNode> list = new ArrayList<FishRPCServerNode>();
		list.addAll(nodeCollection);
		Collections.sort(list, new Comparator<FishRPCServerNode>(){
			@Override
			public int compare(FishRPCServerNode o1, FishRPCServerNode o2) {
				return o1.getAvgeResponseMillsTime() - o2.getAvgeResponseMillsTime();
			}});
		return list.get(0);
	}
	/*
	public static void main(String[] args){
		Map<String,FishRPCServerNode> nodes = java.util.Collections.synchronizedMap(new HashMap<String,FishRPCServerNode>());
		FishRPCServerNode node1 = new FishRPCServerNode();
		node1.setIp("1");
		node1.setPort(2);
		node1.setAvgeResponseMillsTime(1);
		nodes.put(node1.getIp()+":"+node1.getPort(),node1);
		
		FishRPCServerNode node2 = new FishRPCServerNode();
		node2.setIp("2");
		node2.setPort(2);
		node2.setAvgeResponseMillsTime(2);
		nodes.put(node2.getIp()+":"+node2.getPort(),node2);
		
		
		FishRPCServerNode node3 = new FishRPCServerNode();
		node3.setIp("3");
		node3.setPort(2);
		node3.setAvgeResponseMillsTime(3);
		nodes.put(node3.getIp()+":"+node3.getPort(),node3);
		
		
		Collection<FishRPCServerNode> nodeCollection = nodes.values();
		List<FishRPCServerNode> list = new ArrayList<FishRPCServerNode>();
		list.addAll(nodeCollection);
		Collections.sort(list, new Comparator<FishRPCServerNode>(){
			@Override
			public int compare(FishRPCServerNode o1, FishRPCServerNode o2) {
				return o1.getAvgeResponseMillsTime() - o2.getAvgeResponseMillsTime();
			}});
		
		System.out.println(list);
		
		
	}*/
}
