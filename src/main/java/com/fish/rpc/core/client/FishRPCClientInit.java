package com.fish.rpc.core.client;

import java.util.ArrayList;
import java.util.List;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.FishRPCEventListener;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.FishRPCConfig;

public class FishRPCClientInit {
	
	private boolean init = false;
	private static class FishRPCClientInitHolder {
		private static final FishRPCClientInit instance = new FishRPCClientInit();
	}

	public static FishRPCClientInit getInstance() {
		return FishRPCClientInitHolder.instance;
	}

	private FishRPCClientInit() {
		
	}
	
	public  void init(String configPath){
		try {
			if(init){
				return ;
			}
			FishRPCConfig.initClient(configPath);
			initRPCConnection();
			// 注册事件监听器
			AnsyEventBusCenter.getInstance().register(FishRPCEventListener.class);
			init = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initRPCConnection() {
		long start = System.currentTimeMillis();
		System.out.println("Init fishRPC connection...");
		int minConnection = FishRPCConfig.getIntValue("fish.rpc.connect.min", 100);
		System.out.println("Init fishRPC connection,min=" + minConnection);
		List<FishRPCConnection> initConns = new ArrayList<FishRPCConnection>();
		for (int i = 0; i < minConnection; i++) {
			FishRPCConnection connection = FishRPCSendPool.getInstance().borrow();
			initConns.add(connection);
		}
		for (FishRPCConnection conn : initConns) {
			FishRPCSendPool.getInstance().giveBack(conn);
		}
		System.out.println("Init fishRPC connection done ," + (System.currentTimeMillis() - start) + " ms");
	}

}
