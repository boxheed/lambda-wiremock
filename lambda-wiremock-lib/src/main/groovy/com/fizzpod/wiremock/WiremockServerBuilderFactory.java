package com.fizzpod.wiremock;

import java.util.ServiceLoader;

public class WiremockServerBuilderFactory {

	private static WiremockServerBuilder builder = new DefaultWiremockServerBuilder();
	
	private WiremockServerBuilderFactory() {
		
	}
	
	public static WiremockServerBuilder getBuilder() {
		ServiceLoader<WiremockServerBuilder> loader = ServiceLoader.load(WiremockServerBuilder.class);
		return loader.findFirst().orElse(builder);
	}
	
}
