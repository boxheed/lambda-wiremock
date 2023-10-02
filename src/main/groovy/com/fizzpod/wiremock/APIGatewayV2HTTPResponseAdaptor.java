package com.fizzpod.wiremock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.http.HttpHeader;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Response;

public class APIGatewayV2HTTPResponseAdaptor implements ResponseAdaptor<APIGatewayV2HTTPResponse> {

	@Override
	public APIGatewayV2HTTPResponse adapt(Response response) {
		APIGatewayV2HTTPResponse lambdaResponse = new APIGatewayV2HTTPResponse();
		copyStatusCode(response, lambdaResponse);
		copyHeaders(response, lambdaResponse);
		copyCookies(response, lambdaResponse);
		copyBody(response, lambdaResponse);
	    		
		return lambdaResponse;
	}

	private void copyBody(Response response, APIGatewayV2HTTPResponse lambdaResponse) {
		String body = response.getBodyAsString();
		lambdaResponse.setBody(body);
		lambdaResponse.setIsBase64Encoded(false);
		
	}

	private void copyCookies(Response response, APIGatewayV2HTTPResponse lambdaResponse) {
	    List<String> cookies = new LinkedList<>();
	    HttpHeaders responseHeaders = response.getHeaders();
		responseHeaders.all().forEach(t -> {
			if(t.key().equalsIgnoreCase(HttpHeader.COOKIE.asString())) {
				cookies.addAll(t.values());
			}
		});
		lambdaResponse.setCookies(cookies);
	}

	private void copyHeaders(Response response, APIGatewayV2HTTPResponse lambdaResponse) {
		Map<String, String> headers = new HashMap<>();
	    Map<String, List<String>> multiValueHeaders = new HashMap<>();
	    
		HttpHeaders responseHeaders = response.getHeaders();
		responseHeaders.all().forEach(t -> {
				List<String> values = t.values();
				if(values.size() == 1) {
					headers.put(t.key(), values.get(0));
				} else {
					multiValueHeaders.put(t.key(), values);
				}
			});
		lambdaResponse.setHeaders(headers);
		lambdaResponse.setMultiValueHeaders(multiValueHeaders);
	}

	private void copyStatusCode(Response response, APIGatewayV2HTTPResponse lambdaResponse) {
		lambdaResponse.setStatusCode(response.getStatus());
	}

	@Override
	public Class<APIGatewayV2HTTPResponse> getType() {
		return APIGatewayV2HTTPResponse.class;
	}

}
