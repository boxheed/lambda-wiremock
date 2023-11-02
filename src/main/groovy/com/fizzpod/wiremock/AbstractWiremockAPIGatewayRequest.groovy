package com.fizzpod.wiremock;

import java.util.stream.Collectors;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.net.WWWFormCodec

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.FormParameter
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import lombok.NonNull;
import lombok.ToString;

@ToString
public abstract class AbstractWiremockAPIGatewayRequest implements Request {

    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_SCHEME = "https";

    private def event;

    public AbstractWiremockAPIGatewayRequest(@NonNull def event) {
        this.event = event;
    }
	
    public abstract String getUrl() 

    public String getAbsoluteUrl() {
        return this.getUrl();
    }

    public abstract RequestMethod getMethod() 

    public String getScheme() {
        return java.util.Optional.ofNullable(this.getHeader("X-Forwarded-Proto"))
            .map(forwardedHeader -> forwardedHeader.split(","))
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

    public abstract String getClientIp()

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

	public abstract Map<String, Cookie> getCookies()
    

    public abstract QueryParameter queryParameter(String key)

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
        return Optional.empty();
    }

    public abstract String getProtocol()
	
	public FormParameter formParameter(String key) {
		return this.formParameters().get(key);
	}

	public Map<String, FormParameter> formParameters() {
		RequestMethod method = this.getMethod();
		String contentTypeHeader = this.getHeader("Content-Type");
		if(RequestMethod.GET.equals(method)) {
			return java.util.Optional.ofNullable(event)
			.map(event -> event.getQueryStringParameters())
			.map(parameterMap -> {
				def parameters = [:];
				parameterMap.each( entry -> 
					parameters.put(entry.key, new FormParameter(entry.key, Arrays.asList(StringUtils.split(entry.value, ','))))
				)
				return parameters;
			})
			.orElse(Collections.emptyMap());
		} else if(RequestMethod.POST.equals(method) && "application/x-www-form-urlencoded".equals(contentTypeHeader)) {
			Map<String, FormParameter> parameters = new HashMap<>()
			List<NameValuePair> queryString = WWWFormCodec.parse(this.getBodyAsString(), Charset.forName("UTF-8"));
			for(NameValuePair pair: queryString) {
				String name = pair.getName();
				String value = pair.getValue();
				FormParameter parameter = parameters.getOrDefault(name, FormParameter.absent(name))
				List<String> values = new ArrayList<>(parameter.getValues());
				values.add(value);
				parameters.put(name, new FormParameter(name, values))
			}
			return parameters;
		} else if(RequestMethod.POST.equals(method) && "multipart/form-data".equals(contentTypeHeader)) {
			//TODO implement this
		}
		return Collections.emptyMap();
	}

}