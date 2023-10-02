package com.fizzpod.wiremock;

import com.github.tomakehurst.wiremock.http.Request;

public interface RequestAdaptor<T> {

	Request adapt(T request);
	
	Class<T> getType();
	
}
