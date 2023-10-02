package com.fizzpod.wiremock;

import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;

public interface WiremockServerBuilder {

	public DirectCallHttpServer build();
	
}