package com.fizzpod.wiremock;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.eclipse.jetty.server.CookieCutter;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import lombok.NonNull;
import lombok.ToString;

@ToString
public class WiremockAPIGatewayV2HTTPRequest extends AbstractWiremockAPIGatewayRequest  {

    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_SCHEME = "https";

    private final APIGatewayV2HTTPEvent event;

    public WiremockAPIGatewayV2HTTPRequest(@NonNull APIGatewayV2HTTPEvent event) {
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
                            .map(requestContext -> requestContext.getHttp())
                            .map(http -> http.getPath())
                            .orElse("/"));
                    java.util.Optional.ofNullable(event)
                        .map(event -> event.getQueryStringParameters())
                        .map(parameters -> {
                            for(Map.Entry<String, String> entry : parameters.entrySet()) {
                                for(String value: StringUtils.split(entry.getValue(), ',')) {
                                    builder.addParameter(entry.getKey(), value);
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

    public String getAbsoluteUrl() {
        return this.getUrl();
    }

    public RequestMethod getMethod() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getHttp())
            .map(http -> http.getMethod())
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
            .map(requestContext -> requestContext.getHttp())
            .map(http -> http.getSourceIp())
            .orElse(null);
        if(ip == null) {
            ip = java.util.Optional.ofNullable(this.getHeader("X-Forwarded-For"))
                .map(forwardedHeader -> StringUtils.split(forwardedHeader, ','))
                .map(parts -> parts[0].trim())
                .orElse(null);
        }
        return ip;
    }

    
    
    
    
    public Set<String> getAllHeaderKeys() {
        return Collections.unmodifiableSet(
            java.util.Optional.ofNullable(event)
            .map(event -> event.getHeaders())
            .map(headerMap -> headerMap.keySet())
            .map(keys ->
                keys.stream()
                    .filter(key -> getHeader(key) != null)
                    .collect(Collectors.toSet())
            )
            .orElse(Collections.emptySet())
        );
    }

    public Map<String, Cookie> getCookies() {
        Map<String, Cookie> cookies = 
            java.util.Optional.ofNullable(event)
                .map(event -> event.getCookies())
                .map(cookieList -> {
                    CookieCutter cutter = new CookieCutter();
                    for(String cookieValue: cookieList) {
                        cutter.addCookieField(cookieValue);
                    }
                    return cutter.getCookies();
                }).map(cookieArr -> {
                Map<String, Cookie> cookieMap = new HashMap<>();
                for(javax.servlet.http.Cookie cookie: cookieArr) {
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
            .map(event -> event.getQueryStringParameters())
            .map(parameterMap -> parameterMap.get(key))
            .map(parameters ->
                new QueryParameter(key, Arrays.asList(StringUtils.split(parameters, ',')))
            )
            .orElse(null);
    }


    public String getProtocol() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getHttp())
            .map(http -> http.getProtocol())
            .orElse(null);
    }

}