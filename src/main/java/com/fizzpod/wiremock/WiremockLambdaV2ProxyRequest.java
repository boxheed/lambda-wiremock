package com.fizzpod.wiremock;

import lombok.NonNull;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.QueryParameter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

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
public class WiremockLambdaV2ProxyRequest implements Request {

    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_SCHEME = "https";

    private final APIGatewayV2HTTPEvent event;
    //private final APIGatewayProxyRequestEvent event;

    public WiremockLambdaV2ProxyRequest(@NonNull APIGatewayV2HTTPEvent event) {
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

    public String getScheme() {
        return java.util.Optional.ofNullable(this.getHeader("X-Forwarded-Proto"))
            .map(forwardedHeader -> StringUtils.split(forwardedHeader, ','))
            .map(parts -> parts.length > 0? parts[0]: null)
            .map(scheme -> "".equals(scheme.trim())? null: scheme.trim())
            .orElse(DEFAULT_SCHEME);

    }

    public String getHost() {
        String host = java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getDomainName())
            .orElse(null);
        if(host == null) {
            host =  java.util.Optional.ofNullable(this.getHeader("Host"))
                .map(hostHeader -> hostHeader.split(":")[0])
                .map(value -> "".equals(value)? null: value)
                .orElse(null);
        }
        
        return host;
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

    public String getHeader(String key) {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getHeaders())
            .map(headers -> headers.get(key))
            .map(header -> "".equals(header.trim())? null: header.trim())
            .orElse(null);
    }

    public HttpHeader header(String key) {
        return java.util.Optional.ofNullable(getHeader(key))
            .map(value -> new HttpHeader(key, value))
            .orElse(null);
    }

    public ContentTypeHeader contentTypeHeader() {
        return java.util.Optional.ofNullable(getHeader("Content-Type"))
            .map(value -> new ContentTypeHeader(value))
            .orElse(null);
    }

    public HttpHeaders getHeaders() {
        return java.util.Optional.ofNullable(getAllHeaderKeys())
            .map(keys -> keys.stream()
                .map(key -> header(key))
                .collect(Collectors.toList()))
            .map(headerList -> ((List)headerList).size() > 0? new HttpHeaders((List) headerList): HttpHeaders.noHeaders())
            .orElse(HttpHeaders.noHeaders());
    }

    public boolean containsHeader(String key) {
        return this.getHeader(key) != null;
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
            .map(event -> event.getQueryStringParameters())
            .map(parameterMap -> parameterMap.get(key))
            .map(parameters ->
                new QueryParameter(key, Arrays.asList(StringUtils.split(parameters, ',')))
            )
            .orElse(null);
    }

    public byte[] getBody() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getBody())
            .map(body -> body.getBytes())
            .orElse(null);
    }

    public String getBodyAsString() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getBody())
            .orElse(null);
    }

    public String getBodyAsBase64() {
        return java.util.Optional.ofNullable(getBody())
            .map(body -> Base64.getEncoder().encodeToString(body))
            .orElse(null);
    }

    public boolean isMultipart() {
        return false;
    }

    public Collection<Part> getParts() {
        return Collections.emptyList();
    }

    public Part getPart(String name) {
        return null;
    }

    public boolean isBrowserProxyRequest() {
        return true;
    }

    public Optional<Request> getOriginalRequest() {
        return Optional.absent();
    }

    public String getProtocol() {
        return java.util.Optional.ofNullable(event)
            .map(event -> event.getRequestContext())
            .map(requestContext -> requestContext.getHttp())
            .map(http -> http.getProtocol())
            .orElse(null);
    }

}