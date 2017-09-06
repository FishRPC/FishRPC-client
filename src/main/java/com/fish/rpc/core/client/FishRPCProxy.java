package com.fish.rpc.core.client;

import java.lang.reflect.Method;
import java.util.UUID;

import com.fish.rpc.core.event.AnsyEventBusCenter;
import com.fish.rpc.core.event.MessageSendEvent;
import com.fish.rpc.dto.FishRPCRequest;
import com.fish.rpc.dto.FishRPCResponse;
import com.fish.rpc.util.FishRPCLog;
import com.google.common.reflect.AbstractInvocationHandler;

public class FishRPCProxy<T> extends AbstractInvocationHandler {

	@Override
	public Object handleInvocation(Object proxy, Method method, Object[] args) throws Exception {

		FishRPCRequest request = new FishRPCRequest();
		request.setRequestId(UUID.randomUUID().toString());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParamsType(method.getParameterTypes());
		request.setParamsVal(args);

		MessageSendEvent event = new MessageSendEvent(request);
		AnsyEventBusCenter.getInstance().post(event);

		FishRPCResponse response = event.sync();
		
		FishRPCLog.debug("耗时分析：%s", event.elapsedInfo());
		FishRPCLog.info("[FishRPCProxy][handleInvocation][RPC请求响应]\n[ 请求-> %s]\n[ 响应<- %s]",request,  response);

		if( null == response ){
			throw new Exception("请求超时，"+request);
		}
		
		
		if (response.getCode() == 0) {
			return response.getResult();
		}

		if (response.getCode() == -1 && response.getResult() instanceof Exception) {
			throw (Exception) response.getResult();
		}

		return returnDefault(method.getReturnType());
	}

	private Object returnDefault(Class<?> returnType) {
		if (!returnType.isPrimitive()) {
			return null;
		}
		if (returnType == int.class || returnType == short.class || returnType == long.class
				|| returnType == byte.class) {
			return 0;
		}
		if (returnType == double.class || returnType == float.class) {
			return 0.0;
		}
		if (returnType == char.class) {
			return "";
		}
		return null;
	}
}
