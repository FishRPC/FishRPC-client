package com.fish.rpc.netty.connections;

import java.util.*;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fish.nacos.FishNacos;
import com.fish.rpc.util.FishRPCLog;
import org.apache.commons.lang3.StringUtils;

import com.fish.rpc.util.FishRPCConfig;

public class ConnectionManager {


	private Set<Connection> usables = Collections.synchronizedSet(new HashSet<Connection>());
	private Set<Connection> invalid = Collections.synchronizedSet(new HashSet<Connection>());

	private static class ConnectionManagerHolder{
		public static final ConnectionManager cm = new ConnectionManager();
	}
	private ConnectionManager(){
		String server = FishRPCConfig.getStringValue("fish.rpc.server", "127.0.0.1:5050");
		String[] servers = server.split(",");
		// 如果开启了nacos，则从nacos发现可用服务
		boolean nacosEnable = FishRPCConfig.getBooleanValue("nacos.enable", false);
		if(nacosEnable){
			String serverName = FishRPCConfig.getStringValue("nacos.server.name","");
			String groupName = FishRPCConfig.getStringValue("nacos.server","DEFAULT_GROUP");
			String clusterName = FishRPCConfig.getStringValue("nacos.server.cluster","defaultcluster");
			List<String> clusters = Arrays.asList(clusterName.split(",")) ;
			try {
				List<Instance> instances = FishNacos.findInstances(serverName,groupName,clusters);
				for(Instance instance : instances){
					if(!instance.isEnabled()) continue;
					Connection conn = new Connection(instance.getIp(), instance.getPort());
					conn.connect();
				}
			} catch (NacosException e) {
				FishRPCLog.error(e, e.getMessage());
			}
		}else {
			for (String s : servers) {
				if (StringUtils.isEmpty(s)) continue;

				String[] ssplit = s.split(":");
				if (ssplit.length != 2) continue;

				String ip = ssplit[0].trim();
				int port = Integer.parseInt(ssplit[1].trim());
				Connection conn = new Connection(ip, port);
				conn.connect();
			}
		}
	}

	public static ConnectionManager getInstance(){
		return ConnectionManagerHolder.cm;
	}

	public  Connection  getConnection(){
		int size = usables.size();
		if( size == 0 ){
			return null;
		}
		try{
			/*System.out.println("getConnection sleeping,"+size+","+usables.size());
			Thread.sleep(2000);
			System.out.println("getConnection,"+size+","+usables.size());*/
			Random rand = new Random();
			int index = rand.nextInt(size);
			Connection[] conns = usables.toArray(new Connection[0]);
			return conns[index];
		}catch(Exception e){
			return null;
		}

	}

	public void transfer2invalid(Connection from){
		/*System.out.println("transfer2invalid sleeping");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("transfer2invalid");*/
		if( usables.contains(from) ){
			usables.remove(from);
		}
		invalid.add(from);
	}

	public  void transfer2useable(Connection src){
		if( invalid.contains(src) ){
			invalid.remove(src);
		}
		usables.add(src);
	}

	public int getSize(){
		return usables.size();
	}

	public static void main(String args[]){
		final Connection c = new Connection("1",1);
		ConnectionManager.getInstance().transfer2useable(c);


		while(true){
			Thread t2 = new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("--------------2----------------");
					ConnectionManager.getInstance().transfer2invalid(c);
				}
			});

			Thread t1 = new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("--------------1----------------");
					System.out.println(ConnectionManager.getInstance().getConnection());
				}
			});

			Thread t3 = new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("--------------3----------------");
					ConnectionManager.getInstance().transfer2useable(c);
				}
			});

			Thread t4 = new Thread(new Runnable(){
				@Override
				public void run() {
					System.out.println("--------------4----------------");
					System.out.println(ConnectionManager.getInstance().getSize());
				}
			});

			t1.start();t2.start();t3.start();t4.start();
		}
	}
}
