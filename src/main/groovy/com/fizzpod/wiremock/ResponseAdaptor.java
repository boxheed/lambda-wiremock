package com.fizzpod.wiremock;

import com.github.tomakehurst.wiremock.http.Response;

public interface ResponseAdaptor<T> {

	T adapt(Response request);
	
	Class<T> getType();
	
}
