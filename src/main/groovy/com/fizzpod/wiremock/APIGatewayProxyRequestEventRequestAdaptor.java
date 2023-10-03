package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.tomakehurst.wiremock.http.Request;

public class APIGatewayProxyRequestEventRequestAdaptor implements RequestAdaptor<APIGatewayProxyRequestEvent> {

	@Override
	public Request adapt(APIGatewayProxyRequestEvent request) {
		return new WiremockAPIGatewayProxyRequest(request);
	}

	@Override
	public Class<APIGatewayProxyRequestEvent> getType() {
		return APIGatewayProxyRequestEvent.class;
	}

}
