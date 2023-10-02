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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class WiremockAPIGatewayPV2HTTRequestTest {

    @Test
    public void testConstructorWithNulls() {
        assertThrows(NullPointerException.class, () -> {
            WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(null);
        });
    }

    @Test
    public void testToString() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        APIGatewayV2HTTPEvent.RequestContext  requestContext = new APIGatewayV2HTTPEvent.RequestContext ();
        event.setRequestContext(requestContext);
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        requestContext.setHttp(http);
        assertNotNull(request.toString());
    }

    @Test
    public void testNullValuesInEvent() {
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(new APIGatewayV2HTTPEvent());
        assertNull(request.getUrl());
        assertNull(request.getAbsoluteUrl());
        assertNull(request.getMethod());
        assertEquals("https", request.getScheme());
        assertNull(request.getHost());
        assertEquals(443, request.getPort());
        assertNull(request.getClientIp());
        assertNull(request.getHeader(null));
        assertNull(request.getHeader(""));
        assertNull(request.getHeader("abc"));
        assertNull(request.header(null));
        assertNull(request.header(""));
        assertNull(request.header("abc"));
        assertNull(request.contentTypeHeader());
        assertEquals(0, request.getHeaders().size());
        assertFalse(request.containsHeader(null));
        assertFalse(request.containsHeader(""));
        assertFalse(request.containsHeader("abc"));
        assertNotNull(request.getAllHeaderKeys());
        assertEquals(0, request.getAllHeaderKeys().size());
        assertNotNull(request.getCookies());
        assertEquals(0, request.getCookies().size());
        assertNull(request.queryParameter(null));
        assertNull(request.queryParameter(""));
        assertNull(request.queryParameter("abc"));
        assertNull(request.getBody());
        assertNull(request.getBodyAsString());
        assertNull(request.getBodyAsBase64());
        assertFalse(request.isMultipart());
        assertNotNull(request.getParts());
        assertNull(request.getPart(null));
        assertNull(request.getPart(""));
        assertNull(request.getPart("abc"));
        assertTrue(request.isBrowserProxyRequest());
        assertEquals(Optional.absent(), request.getOriginalRequest());
        assertNull(request.getProtocol());
    }

    @Test
    public void testGetMethod() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getMethod());
        APIGatewayV2HTTPEvent.RequestContext  requestContext = new APIGatewayV2HTTPEvent.RequestContext ();
        event.setRequestContext(requestContext);
        assertNull(request.getMethod());
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        requestContext.setHttp(http);
        assertNull(request.getMethod());
        http.setMethod("");
        assertNull(request.getMethod());
        http.setMethod("   ");
        assertNull(request.getMethod());
        http.setMethod("GET");
        assertEquals(RequestMethod.GET, request.getMethod());
        http.setMethod("  GET  ");
        assertEquals(RequestMethod.GET, request.getMethod());
        http.setMethod("BANANA");
        assertNotNull(request.getMethod());

    }

    @Test
    public void testGetScheme() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", "http");
        assertEquals("http", request.getScheme());
        headers.put("X-Forwarded-Proto", "http, https");
        assertEquals("http", request.getScheme());
        headers.put("X-Forwarded-Proto", "");
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", null);
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", ",");
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", "  ,");
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", "  ,  ");
        assertEquals("https", request.getScheme());
        headers.put("X-Forwarded-Proto", "http,  ");
        assertEquals("http", request.getScheme());
        headers.put("X-Forwarded-Proto", "  , http");
        assertEquals("https", request.getScheme());

    }

    @Test
    public void testGetHost() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getHost());
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getHost());
        requestContext.setDomainName("cheese");
        assertEquals("cheese", request.getHost());
        requestContext.setDomainName(null);
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertNull(request.getHost());
        headers.put("Host", null);
        assertNull(request.getHost());
        headers.put("Host", "");
        assertNull(request.getHost());
        headers.put("Host", "grapes");
        assertEquals("grapes", request.getHost());
        headers.put("Host", "lemons:9191");
        assertEquals("lemons", request.getHost());
        headers.put("Host", ":9191");
        assertNull(request.getHost());
    }

    @Test
    public void testGetPort() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertEquals(443, request.getPort());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertEquals(443, request.getPort());
        headers.put("Host", "grapes");
        assertEquals(443, request.getPort());
        headers.put("Host", "lemons:9191");
        assertEquals(9191, request.getPort());
    }

    @Test
    public void testGetClientIp() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getClientIp());
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getClientIp());
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        requestContext.setHttp(http);
        http.setSourceIp("192.168.1.1");
        assertEquals("192.168.1.1", request.getClientIp());
        http.setSourceIp(null);
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertNull(request.getClientIp());
        headers.put("X-Forwarded-For", "192.168.1.1");
        assertEquals("192.168.1.1", request.getClientIp());
        headers.put("X-Forwarded-For", "192.168.1.1, 100.101.102.103");
        assertEquals("192.168.1.1", request.getClientIp());
    }

    @Test
    public void testGetHeader() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getHeader(null));
        assertNull(request.getHeader(""));
        assertNull(request.getHeader("xxx"));
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertNull(request.getHeader(null));
        assertNull(request.getHeader(""));
        assertNull(request.getHeader("xxx"));
        headers.put("abc", null);
        assertNull(request.getHeader("abc"));
        headers.put("abc", "");
        assertNull(request.getHeader("abc"));
        headers.put("abc", "xyz");
        assertEquals("xyz", request.getHeader("abc"));
        headers.put("abc", "   def   ");
        assertEquals("def", request.getHeader("abc"));

    }



    @Test
    public void testHeader() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.header(null));
        assertNull(request.header(""));
        assertNull(request.header("xxx"));
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertNull(request.header(null));
        assertNull(request.header(""));
        assertNull(request.header("xxx"));
        headers.put("abc", null);
        assertNull(request.header("abc"));
        headers.put("abc", "");
        assertNull(request.header("abc"));
        headers.put("abc", "xyz");
        assertEquals(new HttpHeader("abc", "xyz"), request.header("abc"));
        headers.put("abc", "   def   ");
        assertEquals(new HttpHeader("abc", "def"), request.header("abc"));
    }

    @Test
    public void testContentTypeHeader() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.contentTypeHeader());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        headers.put("Content-Type", null);
        assertNull(request.contentTypeHeader());
        headers.put("Content-Type", "");
        assertNull(request.contentTypeHeader());
        headers.put("Content-Type", "abcdefg");
        assertEquals(new ContentTypeHeader("abcdefg"), request.contentTypeHeader());
    }

    @Test
    public void testGetHeaders() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        headers.put("abc", null);
        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        headers.put("abc", "");
        assertNotNull(request.getHeaders());
        assertEquals(0, request.getHeaders().size());
        headers.put("abc", "def");
        assertNotNull(request.getHeaders());
        assertEquals(1, request.getHeaders().size());
        assertEquals(new HttpHeader("abc", "def"), request.getHeaders().getHeader("abc"));
        headers.put("jdk", null);
        headers.put("jre", "");
        headers.put("sdk", " ");
        assertNotNull(request.getHeaders());
        assertEquals(1, request.getHeaders().size());

    }

    @Test
    public void testContainsHeader() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertFalse(request.containsHeader(null));
        assertFalse(request.containsHeader(""));
        assertFalse(request.containsHeader("   "));
        assertFalse(request.containsHeader("xxx"));
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertFalse(request.containsHeader(null));
        assertFalse(request.containsHeader(""));
        assertFalse(request.containsHeader("xxx"));
        headers.put("abc", null);
        assertFalse(request.containsHeader("abc"));
        headers.put("abc", "");
        assertFalse(request.containsHeader("abc"));
        headers.put("abc", "    ");
        assertFalse(request.containsHeader("abc"));
        headers.put("abc", "xyz");
        assertTrue(request.containsHeader("abc"));
        headers.put("abc", "   def   ");
        assertTrue(request.containsHeader("abc"));

    }

    @Test
    public void testGetAllHeaderKeys() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNotNull(request.getAllHeaderKeys());
        assertEquals(0, request.getAllHeaderKeys().size());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertEquals(0, request.getAllHeaderKeys().size());
        headers.put("abc", null);
        assertEquals(0, request.getAllHeaderKeys().size());
        headers.put("abc", "");
        assertEquals(0, request.getAllHeaderKeys().size());
        headers.put("abc", "    ");
        assertEquals(0, request.getAllHeaderKeys().size());
        headers.put("abc", "xyz");
        assertEquals(1, request.getAllHeaderKeys().size());
    }


    @Test
    public void testGetCookies() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNotNull(request.getCookies());
        assertEquals(0, request.getCookies().size());
        List<String> cookies = new ArrayList<>();
        event.setCookies(cookies);
        assertEquals(0, request.getCookies().size());
        cookies.add("abc=def");
        assertEquals(1, request.getCookies().size());
        assertEquals("def", request.getCookies().get("abc").getValue());
        cookies.clear();
        cookies.add("theme=light");
        cookies.add("sessionToken=abc123");
        assertEquals(2, request.getCookies().size());
        cookies.clear();
        cookies.add("theme=dark");
        cookies.add("theme=light");
        cookies.add("identity=abc123");
        assertEquals(2, request.getCookies().size());
        Cookie themeCookie = request.getCookies().get("theme");
        assertNotNull(themeCookie);
        assertEquals(2, themeCookie.getValues().size());
        assertTrue(themeCookie.getValues().contains("dark"));
        assertTrue(themeCookie.getValues().contains("light"));
        Cookie identityCookie = request.getCookies().get("identity");
        assertNotNull(identityCookie);
        assertEquals(1, identityCookie.getValues().size());
        assertTrue(identityCookie.getValues().contains("abc123"));
    }

    @Test
    public void testQueryParameter() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.queryParameter("abc"));
        Map<String, String> parameters = new HashMap<>();
        event.setQueryStringParameters(parameters);
        assertNull(request.queryParameter("abc"));
        parameters.put("abc", "def");
        assertNotNull(request.queryParameter("abc"));
        assertEquals(1, request.queryParameter("abc").values().size());
        parameters.put("hij", "klm,nop");
        assertNotNull(request.queryParameter("hij"));
        assertEquals(2, request.queryParameter("hij").values().size());
    }

    @Test
    public void testGetBody() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getBody());
        event.setBody("");
        assertEquals(0, request.getBody().length);
        event.setBody("abc123");
        assertArrayEquals("abc123".getBytes(), request.getBody());
    }

    @Test
    public void testGetBodyAsString() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getBodyAsString());
        event.setBody("");
        assertEquals("", request.getBodyAsString());
        event.setBody("abc");
        assertEquals("abc", request.getBodyAsString());
    }

    @Test
    public void testGetBodyAsBase64() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getBodyAsBase64());
        event.setBody("");
        assertEquals("", request.getBodyAsBase64());
        event.setBody("abc");
        assertEquals("YWJj", request.getBodyAsBase64());
    }

    @Test
    public void testGetProtocol() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getProtocol());
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getProtocol());
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        requestContext.setHttp(http);
        assertNull(request.getProtocol());
        http.setProtocol("HTTP/1.1");
        assertEquals("HTTP/1.1", request.getProtocol());
    }

    @Test
    public void testGetUrl() {
        var event = new APIGatewayV2HTTPEvent();
        WiremockAPIGatewayV2HTTPRequest request = new WiremockAPIGatewayV2HTTPRequest(event);
        assertNull(request.getUrl());
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getUrl());
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        requestContext.setHttp(http);
        assertNull(request.getUrl());

        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        headers.put("X-Forwarded-Proto", "http");
        headers.put("Host", "www.example.com:9191");
        http.setPath("/this/is/a/path");
        Map<String, String> parameters = new HashMap<>();
        event.setQueryStringParameters(parameters);
        parameters.put("abc", "def");
        assertEquals("http://www.example.com:9191/this/is/a/path?abc=def", request.getUrl());
    }
    

}