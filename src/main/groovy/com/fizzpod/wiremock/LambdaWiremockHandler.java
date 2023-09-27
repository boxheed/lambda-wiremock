package com.fizzpod.wiremock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServerFactory;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.HashMap;

public class LambdaWiremockHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private DirectCallHttpServerFactory factory;
    private WireMockServer wm;
    private DirectCallHttpServer server;

    public LambdaWiremockHandler() {
        this.factory = new DirectCallHttpServerFactory();
        var config = wireMockConfig()
                .httpServerFactory(factory)
                .usingFilesUnderClasspath("wiremock")
                .notifier(new ConsoleNotifier(true));
        this.wm = new WireMockServer(config);
        wm.start(); 
        server = this.factory.getHttpServer();
    }


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("EVENT TYPE: " + event.getClass().toString());
        Request wiremockRequest = new WiremockAPIGatewayV2HTTPRequest(event);
        Response wiremockResponse = server.stubRequest(wiremockRequest);

        System.out.println("Wiremock Response: " + wiremockResponse);

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/html");
        response.setHeaders(headers);
        String body = event.getBody() != null ? event.getBody() : "Empty body";
        response.setBody("<!DOCTYPE html><html><head><title>" + body + "</title></head><body>" +
        "<h1>Welcome</h1><p>Page generated by a Lambda function.</p>" +
        "</body></html>");
        return response;
    }

}