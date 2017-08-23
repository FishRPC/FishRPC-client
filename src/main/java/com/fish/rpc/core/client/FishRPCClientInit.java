package com.fish.rpc.core.client;

import java.util.ArrayList;
import java.util.List;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.FishRPCEventListener;
import com.fish.rpc.netty.pool.FishRPCConnection;
import com.fish.rpc.netty.pool.FishRPCSendPool;
import com.fish.rpc.util.FishRPCConfig;
import com.fish.rpc.util.FishRPCLog;

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
		int minConnection = FishRPCConfig.getIntValue("fish.rpc.connect.min", 100);
		FishRPCLog.info("[FishRPCClientInit][initRPCConnection][RPC连接初始化][最小连接数：%s ms]",minConnection);
		List<FishRPCConnection> initConns = new ArrayList<FishRPCConnection>();
		for (int i = 0; i < minConnection; i++) {
			FishRPCConnection connection = FishRPCSendPool.getInstance().borrow();
			if( connection == null ){
				continue;
			}
			initConns.add(connection);
		}
		for (FishRPCConnection conn : initConns) {
			FishRPCSendPool.getInstance().giveBack(conn);
		}
		FishRPCLog.info("[FishRPCClientInit][initRPCConnection][RPC连接初始化完成][耗时：%s]",(System.currentTimeMillis() - start));
 	}

}
