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

import java.util.Optional;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.QueryParameter;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WiremockAPIGatewayProxyRequestTest {

    @Test
    public void testConstructorWithNulls() {
        assertThrows(NullPointerException.class, () -> {
            WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(null);
        });
    }

    @Test
    public void testToString() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        event.setRequestContext(requestContext);
        APIGatewayProxyRequestEvent.RequestIdentity requestIdentity = new APIGatewayProxyRequestEvent.RequestIdentity();
        requestContext.setIdentity(requestIdentity);
        assertNotNull(request.toString());
    }

    @Test
    public void testNullValuesInEvent() {
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(new APIGatewayProxyRequestEvent());
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
        assertEquals(Optional.empty(), request.getOriginalRequest());
        assertNull(request.getProtocol());
    }

    @Test
    public void testGetMethod() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getMethod());
        event.setHttpMethod("");
        assertNull(request.getMethod());
        event.setHttpMethod("   ");
        assertNull(request.getMethod());
        event.setHttpMethod("GET");
        assertEquals(RequestMethod.GET, request.getMethod());
        event.setHttpMethod("  GET  ");
        assertEquals(RequestMethod.GET, request.getMethod());
        event.setHttpMethod("BANANA");
        assertNotNull(request.getMethod());

    }

    @Test
    public void testGetScheme() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getHost());
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getClientIp());
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getClientIp());
        APIGatewayProxyRequestEvent.RequestIdentity requestIdentity = new APIGatewayProxyRequestEvent.RequestIdentity();
        requestContext.setIdentity(requestIdentity);
        requestIdentity.setSourceIp("192.168.1.1");
        assertEquals("192.168.1.1", request.getClientIp());
        requestIdentity.setSourceIp(null);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNotNull(request.getCookies());
        assertEquals(0, request.getCookies().size());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        assertEquals(0, request.getCookies().size());
        headers.put("Cookie", "abc=def");
        assertEquals(1, request.getCookies().size());
        assertEquals("def", request.getCookies().get("abc").getValue());
        headers.put("Cookie", "theme=light; sessionToken=abc123");
        assertEquals(2, request.getCookies().size());
        headers.put("Cookie", "theme=dark; theme=light; identity=abc123");
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
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.queryParameter("abc"));
        Map<String, List<String>> parameters = new HashMap<>();
        event.setMultiValueQueryStringParameters(parameters);
        assertNull(request.queryParameter("abc"));
        parameters.put("abc", Arrays.asList("def"));
        assertNotNull(request.queryParameter("abc"));
        assertEquals(1, request.queryParameter("abc").values().size());
    }

    @Test
    public void testGetBody() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getBody());
        event.setBody("");
        assertEquals(0, request.getBody().length);
        event.setBody("abc123");
        assertArrayEquals("abc123".getBytes(), request.getBody());
    }

    @Test
    public void testGetBodyAsString() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getBodyAsString());
        event.setBody("");
        assertEquals("", request.getBodyAsString());
        event.setBody("abc");
        assertEquals("abc", request.getBodyAsString());
    }

    @Test
    public void testGetBodyAsBase64() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getBodyAsBase64());
        event.setBody("");
        assertEquals("", request.getBodyAsBase64());
        event.setBody("abc");
        assertEquals("YWJj", request.getBodyAsBase64());
    }

    @Test
    public void testGetProtocol() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getProtocol());
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getProtocol());
        requestContext.setProtocol("HTTP/1.1");
        assertEquals("HTTP/1.1", request.getProtocol());
    }

    @Test
    public void testGetUrl() {
        var event = new APIGatewayProxyRequestEvent();
        WiremockAPIGatewayProxyRequest request = new WiremockAPIGatewayProxyRequest(event);
        assertNull(request.getUrl());
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        event.setRequestContext(requestContext);
        assertNull(request.getUrl());
        Map<String, String> headers = new HashMap<>();
        event.setHeaders(headers);
        headers.put("X-Forwarded-Proto", "http");
        headers.put("Host", "www.example.com:9191");
        requestContext.setPath("/this/is/a/path");
        Map<String, List<String>> parameters = new HashMap<>();
        event.setMultiValueQueryStringParameters(parameters);
        parameters.put("abc", Arrays.asList("def"));
        assertEquals("http://www.example.com:9191/this/is/a/path?abc=def", request.getUrl());
    }
    

}