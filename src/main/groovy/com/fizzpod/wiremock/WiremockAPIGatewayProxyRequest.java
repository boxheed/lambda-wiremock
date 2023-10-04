package com.fizzpod.wiremock;

import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.net.URIBuilder;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import lombok.NonNull;
import lombok.ToString;

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
            	List<HttpCookie> httpCookieList = new ArrayList<>();
            	String[] cookieCrumbs = cookieValue.split(";");
    			for(String crumb: cookieCrumbs) {
    				httpCookieList.addAll(HttpCookie.parse(crumb));
    			}
            	return httpCookieList;
            }).map(cookieArr -> {
                Map<String, Cookie> cookieMap = new HashMap<>();
                for(HttpCookie cookie: cookieArr) {
                    String name = cookie.getName();
                    List<String> values = new ArrayList<>();
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