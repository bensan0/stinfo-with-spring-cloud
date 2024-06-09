package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

    @PostMapping(value = "/feign/stock/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveAll(@RequestBody List<DailyStockInfoDto> data, @RequestHeader("token") String token);
}
