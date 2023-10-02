package com.fizzpod.wiremock;

import lombok.NonNull;
import lombok.ToString;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.QueryParameter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import com.google.common.base.Optional;

import org.eclipse.jetty.server.CookieCutter;
import org.apache.hc.core5.net.URIBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Base64;
import java.net.URISyntaxException;

@ToString
public class WiremockAPIGatewayProxyRequest extends AbstractWiremockAPIGatewayRequest {

    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_SCHEME = "https";

    private final APIGatewayProxyRequestEvent event;

    public WiremockAPIGatewayProxyRequest(@NonNull APIGatewayProxyRequestEvent event) {
        super(event);
        this.event = event;
    }

    public String getUrl() {
        String url = java.util.Optional.ofNullable(this.getScheme())
            .map(scheme -> this.getHost())
            .map(host -> this.getPort())
            .map(port -> new URIBuilder())
            .map(builder -> {
                try {
                    builder.setScheme(this.getScheme())
                        .setHost(this.getHost())
                        .setPort(this.getPort())
                        .setPath(java.util.Optional.ofNullable(event)
                            .map(event -> event.getRequestContext())
                            .map(requestContext -> requestContext.getPath())
                            .orElse("/"));
                    java.util.Optional.ofNullable(event)
                        .map(event -> event.getMultiValueQueryStringParameters())
                        .map(parameters -> {
                            for(String key: parameters.keySet()) {
                                for(String value: parameters.get(key)) {
                                    builder.addParameter(key, value);
                                }
                            }
                            return builder;
                        });
                    return builder.build().toString();
                } catch (URISyntaxException e) {
                    //TODO log this
                }
                return null;
            })
            .orElse(null);
        return url;
    }

    public RequestMethod getMethod() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getHttpMethod())
            .map(method -> "".equals(method.trim())? null: method.trim())
            .map(method -> RequestMethod.fromString(method.trim()))
            .orElse(null);
    }

    public int getPort() {
        return java.util.Optional.ofNullable(this.getHeader("Host"))
            .map(hostHeader -> hostHeader.split(":"))
            .map(hostParts -> hostParts.length > 1? Integer.valueOf(hostParts[1]): DEFAULT_PORT)
            .orElse(DEFAULT_PORT);
    }

    public String getClientIp() {
        String ip = java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getIdentity())
            .map(identity -> identity.getSourceIp())
            .orElse(null);
        if(ip == null) {
            ip = java.util.Optional.ofNullable(this.getHeader("X-Forwarded-For"))
                .map(forwardedHeader -> forwardedHeader.split(","))
                .map(parts -> parts[0].trim())
                .orElse(null);
        }
        return ip;
    }

    public Map<String, Cookie> getCookies() {
        Map<String, Cookie> cookies = 
        java.util.Optional.ofNullable(this.getHeader("Cookie"))
            .map(cookieValue -> {
                CookieCutter cutter = new CookieCutter();
                cutter.addCookieField(cookieValue);
                return cutter.getCookies();
            }).map(cookieArr -> {
                Map<String, Cookie> cookieMap = new HashMap<>();
                for(javax.servlet.http.Cookie cookie: cookieArr) {
                    String name = cookie.getName();
                    List<String> values = new ArrayList();
                    values.add(cookie.getValue());
                    if(cookieMap.containsKey(name)) {
                        Cookie c = cookieMap.get(name);
                        values.addAll(c.getValues());
                    }
                    Cookie newCookie = new Cookie(name, values);
                    cookieMap.put(name, newCookie);
                }
                return cookieMap;
            }).orElse(Collections.emptyMap());
            
        return Collections.unmodifiableMap(cookies);
    }

    public QueryParameter queryParameter(String key) {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getMultiValueQueryStringParameters())
            .map(parameterMap -> parameterMap.get(key))
            .map(parameters ->
                new QueryParameter(key, parameters)
            )
            .orElse(null);
    }

    public String getProtocol() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getProtocol())
            .orElse(null);
    }

}