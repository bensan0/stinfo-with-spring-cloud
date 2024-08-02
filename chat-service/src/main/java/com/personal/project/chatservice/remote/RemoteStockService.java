package com.personal.project.chatservice.remote;

import com.personal.project.chatservice.model.FRealTimeStockDTO;
import com.personal.project.chatservice.service.argsprocesser.RemoteRequestDTO;
import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

	@PostMapping(value = "/feign/stock/condition/real-time/list")
	CommonResponse<List<FRealTimeStockDTO>> conditionFRealTimeQuery(@RequestBody RemoteRequestDTO dto);

}
