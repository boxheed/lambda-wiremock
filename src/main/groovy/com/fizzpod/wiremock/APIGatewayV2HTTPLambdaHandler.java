package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;

public class APIGatewayV2HTTPLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private DirectCallHttpServer server;
    
    private RequestAdaptor<APIGatewayV2HTTPEvent> requestAdaptor;
    
    private ResponseAdaptor<APIGatewayV2HTTPResponse> responseAdaptor;

    
    public APIGatewayV2HTTPLambdaHandler() {
    	this.server = WiremockServerBuilderFactory.getBuilder().build();
    	this.requestAdaptor = AdaptorFactory.getRequestAdaptor(APIGatewayV2HTTPEvent.class);
    	this.responseAdaptor = AdaptorFactory.getResponseAdaptor(APIGatewayV2HTTPResponse.class);
    }

    @Override
    @Logging(logEvent = true, correlationIdPath = CorrelationIdPathConstants.API_GATEWAY_REST)
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Request wiremockRequest = requestAdaptor.adapt(event);
        Response wiremockResponse = server.stubRequest(wiremockRequest);
        APIGatewayV2HTTPResponse response = responseAdaptor.adapt(wiremockResponse);
        return response;
    }

}