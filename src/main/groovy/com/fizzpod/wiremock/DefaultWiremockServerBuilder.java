package com.fizzpod.wiremock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServerFactory;

public class DefaultWiremockServerBuilder implements WiremockServerBuilder {

	private DirectCallHttpServerFactory factory;
    private WireMockServer wm;
    private DirectCallHttpServer server;
	
	protected DefaultWiremockServerBuilder() {
		
	}
	
	public synchronized DirectCallHttpServer build() {
		if(server == null) {
			this.factory = new DirectCallHttpServerFactory();
	        var config = wireMockConfig()
	                .httpServerFactory(factory)
	                .usingFilesUnderClasspath("wiremock")
	                .notifier(new Slf4jNotifier(true));
	        this.wm = new WireMockServer(config);
	        wm.start(); 
	        server = this.factory.getHttpServer();
		}
        return server;
	}
	
}
