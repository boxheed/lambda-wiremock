package com.fizzpod.wiremock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import com.google.common.base.Optional;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.QueryParameter;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.amazonaws.services.lambda.runtime.tests.EventLoader;


import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.Mockito.*;

public class LambdaWiremockHandlerTest {

    private Context context;

    private APIGatewayV2HTTPEvent event;
    
    @BeforeEach
    public void setupContext() {
        context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        event = EventLoader.loadApiGatewayHttpEvent("apigateway/requests/01_example.json");
        System.out.println(event);
    }

    @Test
    public void testExampleEventMapping() {
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertEquals("https://id.execute-api.us-east-1.amazonaws.com:443/api/v1/scans/22c5/files/9mrz", request.getUrl());
    }

    @Test
    public void createLambdaServer() {
        LambdaWiremockHandler handler = new LambdaWiremockHandler();
    }

    @Test
    public void sendRequest() {
        LambdaWiremockHandler handler = new LambdaWiremockHandler();
        handler.handleRequest(event, context);
    }

}