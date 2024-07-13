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

@FeignClient(contextId = "remoteReportService", value = ServiceNameConstants.REPORT_SERVICE, fallbackFactory = RemoteReportFallbackFactory.class)
public interface RemoteReportService {

    @PostMapping(value = "/feign/report/init-yesterday-report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> initYesterdayReport(@RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/report/init-today-report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> initTodayReport(@RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/report/gen-real-time-metrics-report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> genRealTimeMetricsReport(@RequestBody Long date, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/report/gen-real-time-detail-report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> genRealTimeDetailReport(@RequestBody Long date, @RequestHeader(name = "token") String token);
}
