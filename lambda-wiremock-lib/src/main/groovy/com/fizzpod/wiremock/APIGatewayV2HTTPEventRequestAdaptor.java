package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.github.tomakehurst.wiremock.http.Request;

public class APIGatewayV2HTTPEventRequestAdaptor implements RequestAdaptor<APIGatewayV2HTTPEvent> {

	@Override
	public Request adapt(APIGatewayV2HTTPEvent request) {
		return new WiremockAPIGatewayV2HTTPRequest(request);
	}
	
	@Override
	public Class<APIGatewayV2HTTPEvent> getType() {
		return APIGatewayV2HTTPEvent.class;
	}

}
