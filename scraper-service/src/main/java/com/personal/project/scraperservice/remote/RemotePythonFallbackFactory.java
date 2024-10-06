package com.personal.project.scraperservice.remote;

import org.springframework.cloud.openfeign.FallbackFactory;

public class RemotePythonFallbackFactory implements FallbackFactory<RemotePythonService> {
	@Override
	public RemotePythonService create(Throwable cause) {
		return null;
	}
}
