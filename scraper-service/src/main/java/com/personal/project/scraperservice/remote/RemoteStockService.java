package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

    @GetMapping(value = "/feign/stock/get-by-date")
    InnerResponse<Map<String, DailyStockInfoDto>> getByDate(@RequestParam Long date, @RequestHeader("token") String token);

    @PostMapping(value = "/feign/stock/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveAll(@RequestBody List<DailyStockInfoDto> data, @RequestHeader("token") String token);

    @PostMapping(value = "/feign/stock/init/save-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> initSaveAll(@RequestBody List<DailyStockInfoDto> initData, @RequestHeader("token") String token);

    @GetMapping(value = "/feign/stock/get-former", consumes = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<Map<String, DailyStockInfoDto>> getFormer(@RequestParam Long date, @RequestHeader("token") String token);

    @GetMapping(value = "/feign/stock/get-exists", consumes = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<List<String>> getExist();
}
