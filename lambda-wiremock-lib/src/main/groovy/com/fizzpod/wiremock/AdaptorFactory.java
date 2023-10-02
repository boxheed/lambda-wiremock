package com.fizzpod.wiremock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AdaptorFactory {

	private static final Map<Class<?>, RequestAdaptor<? extends Object>> requestAdaptors = new HashMap<>();
	static {
		requestAdaptors.put(APIGatewayV2HTTPEvent.class, new APIGatewayV2HTTPEventRequestAdaptor());
		requestAdaptors.put(APIGatewayProxyRequestEvent.class, new APIGatewayProxyRequestEventRequestAdaptor());
		ServiceLoader<RequestAdaptor> loader = ServiceLoader.load(RequestAdaptor.class);
		loader.forEach(t -> requestAdaptors.put(t.getType(), t));
	}

	private static final Map<Class<?>, ResponseAdaptor<? extends Object>> responseAdaptors = new HashMap<>();
	static {
		responseAdaptors.put(APIGatewayV2HTTPResponse.class, new APIGatewayV2HTTPResponseAdaptor());
		// responseAdaptors.put(APIGatewayProxyRequestEvent.class, new
		// APIGatewayProxyRequestEventRequestAdaptor());
		ServiceLoader<ResponseAdaptor> loader = ServiceLoader.load(ResponseAdaptor.class);
		loader.forEach(t -> responseAdaptors.put(t.getType(), t));
	}

	private AdaptorFactory() {

	}

	public static <T> RequestAdaptor<T> getRequestAdaptor(Class<T> requestClazz) {
		return (RequestAdaptor<T>) Optional.ofNullable(requestAdaptors.get(requestClazz))
				.orElseThrow(() -> new RuntimeException("Could not find adaptor for " + requestClazz));
	}

	public static <T> ResponseAdaptor<T> getResponseAdaptor(Class<T> responseClazz) {
		return (ResponseAdaptor<T>) Optional.ofNullable(responseAdaptors.get(responseClazz))
				.orElseThrow(() -> new RuntimeException("Could not find adaptor for " + responseClazz));
	}
}
