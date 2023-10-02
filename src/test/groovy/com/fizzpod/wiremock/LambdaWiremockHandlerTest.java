package com.fizzpod.wiremock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.tests.EventLoader;

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
        APIGatewayV2HTTPLambdaHandler handler = new APIGatewayV2HTTPLambdaHandler();
    }

    @Test
    public void sendRequest() {
        APIGatewayV2HTTPLambdaHandler handler = new APIGatewayV2HTTPLambdaHandler();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);
        assertEquals(200, response.getStatusCode());
        assertEquals(3, response.getHeaders().size());
        assertTrue(response.getHeaders().containsKey("Content-Type"));
        assertEquals(response.getHeaders().get("Content-Type"), "application/json");
        assertEquals("{\n  \"component_whitelist_id\" : \"\",\n  \"created_at\" : \"2021-03-13T15:35:37.022Z\",\n  \"current_page\" : 1,\n  \"dependencies\" : [ {\n    \"comparator\" : \"=\",\n    \"created_at\" : \"2021-03-13T15:35:37.091Z\",\n    \"id\" : \"604cdbc9319f0564a8648677\",\n    \"lang_key\" : \"Ruby/activemodel\",\n    \"lang_keyver\" : \"Ruby/activemodel/6.1.3\",\n    \"lang_name\" : \"Ruby/activemodel\",\n    \"language\" : \"Ruby\",\n    \"license_concatenation\" : \"OR\",\n    \"license_violation\" : false,\n    \"licenses\" : [ {\n      \"name\" : \"MIT\",\n      \"on_component_whitelist\" : false,\n      \"on_license_whitelist\" : true\n    } ],\n    \"name\" : \"activemodel\",\n    \"outdated\" : false,\n    \"prod_key\" : \"activemodel\",\n    \"release\" : false,\n    \"scope\" : \"compile\",\n    \"stability\" : \"\",\n    \"unknown_license\" : false,\n    \"updated_at\" : \"2021-03-13T15:35:37.091Z\",\n    \"version_current\" : \"6.1.3\",\n    \"version_label\" : \"6.1.3\",\n    \"version_requested\" : \"6.1.3\",\n    \"whitelisted\" : false\n  } ],\n  \"dependencies_count\" : 31,\n  \"dependency_manager\" : \"gem\",\n  \"file_name\" : \"Gemfile.lock\",\n  \"id\" : \"604cdbc9319f0564a8648662\",\n  \"language\" : \"Ruby\",\n  \"license_unknown_count\" : 0,\n  \"license_violations_count\" : 0,\n  \"license_whitelist_id\" : \"5f929939ac7df80001ffeba5\",\n  \"max_pages\" : 1,\n  \"outdated_count\" : 0,\n  \"outdated_perc_count\" : 0,\n  \"parsing_errors\" : [ ],\n  \"per_page\" : 50,\n  \"post_process\" : false,\n  \"scopes\" : [ \"compile\" ],\n  \"sec_count\" : 0,\n  \"sv_count\" : 0,\n  \"unknown_count\" : 0\n}", response.getBody());
    }

}