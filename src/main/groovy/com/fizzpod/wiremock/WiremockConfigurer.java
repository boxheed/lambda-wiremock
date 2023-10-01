package com.fizzpod.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public interface WiremockConfigurer {

	WireMockConfiguration configure();
	
}
