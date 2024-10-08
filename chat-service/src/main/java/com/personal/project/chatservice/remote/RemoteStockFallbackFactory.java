package com.personal.project.chatservice.remote;

import com.personal.project.chatservice.model.FRealTimeStockDTO;
import com.personal.project.chatservice.service.argsprocesser.RemoteRequestDTO;
import com.personal.project.commoncore.response.CommonResponse;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

public class RemoteStockFallbackFactory implements FallbackFactory<RemoteStockService> {
	@Override
	public RemoteStockService create(Throwable cause) {
		return new RemoteStockService() {
			@Override
			public CommonResponse<List<FRealTimeStockDTO>> conditionFRealTimeQuery(RemoteRequestDTO dto) {
				return null;
			}
		};
	}
}
