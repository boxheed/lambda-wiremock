package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.github.tomakehurst.wiremock.http.Response;

public class APIGatewayV2HTTPResponseAdaptor implements ResponseAdaptor<APIGatewayV2HTTPResponse> {

	@Override
	public APIGatewayV2HTTPResponse adapt(Response request) {
		return null;
	}

	@Override
	public Class<APIGatewayV2HTTPResponse> getType() {
		return APIGatewayV2HTTPResponse.class;
	}

}
