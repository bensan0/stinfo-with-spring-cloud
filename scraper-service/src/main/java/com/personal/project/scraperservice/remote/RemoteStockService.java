package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyIndexInfoDTO;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

	@GetMapping(value = "/feign/stock/get-by-date")
	InnerResponse<Map<String, DailyStockInfoDTO>> getByDate(@RequestParam Long date);

	@GetMapping(value = "/feign/stock/index/get-by-date")
	InnerResponse<Map<String, DailyIndexInfoDTO>> getIndexByDate(@RequestParam Long date);

	@PostMapping(value = "/feign/stock/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	InnerResponse<ObjectUtils.Null> saveAll(@RequestBody List<DailyStockInfoDTO> data);

	@PostMapping(value = "/feign/stock/index/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	InnerResponse<ObjectUtils.Null> saveAllIndex(@RequestBody List<DailyIndexInfoDTO> data);

	@PostMapping(value = "/feign/stock/init/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	InnerResponse<ObjectUtils.Null> initSaveAll(@RequestBody List<DailyStockInfoDTO> initData);

	@GetMapping(value = "/feign/stock/get-former", consumes = MediaType.APPLICATION_JSON_VALUE)
	InnerResponse<Map<String, DailyStockInfoDTO>> getFormer(@RequestParam Long date);

	@GetMapping("/feign/stock/init/check")
	InnerResponse<Boolean> checkInit();
}
