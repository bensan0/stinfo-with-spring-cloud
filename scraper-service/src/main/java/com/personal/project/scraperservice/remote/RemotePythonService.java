package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.config.PythonFeignConfig;
import com.personal.project.scraperservice.model.dto.PyIndexDTO;
import com.personal.project.scraperservice.model.dto.PyStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(contextId = "remotePythonService", value = ServiceNameConstants.PYTHON_SERVICE, fallbackFactory = RemotePythonFallbackFactory.class, configuration = PythonFeignConfig.class)
public interface RemotePythonService {

	@GetMapping(value = "/job/crawl_tx", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	InnerResponse<String> tx();

	@GetMapping(value = "/job/craw_realtime")
	InnerResponse<List<PyStockDTO>> realtimePy();

	@GetMapping(value = "/job/crawl_routine")
	InnerResponse<List<PyStockDTO>> routinePy();

	@GetMapping(value = "/job/crawl_index")
	InnerResponse<List<PyIndexDTO>> indexPy();
}
