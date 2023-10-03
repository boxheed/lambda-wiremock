package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.tests.EventLoader;


public class Main {

    public static void main(String[] args) {
        APIGatewayV2HTTPLambdaHandler handler = new APIGatewayV2HTTPLambdaHandler();
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event = EventLoader.loadApiGatewayHttpEvent("example.json");
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, null);
        System.out.println(response);
    }

}