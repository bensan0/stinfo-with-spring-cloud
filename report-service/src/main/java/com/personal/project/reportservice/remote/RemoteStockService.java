package com.personal.project.reportservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

    @PostMapping(value = "/feign/stock/query-4-cal-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(@RequestBody Query4CalMetricsDTO query4CalMetricsDto, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/manual/query-4-cal-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(@RequestBody Query4ManualCalDTO query4ManualCalDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/save-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveMetrics(@RequestBody List<DailyStockMetricsDTO> metrics, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/query-4-cal-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalDetailUnionDTO> getCalDetailInfo(@RequestBody Query4CalMetricsDTO query4CalMetricsDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/manual/query-4-cal-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalDetailUnionDTO> getCalDetailInfo(@RequestBody Query4ManualCalDTO Query4ManualCalDTO, @RequestHeader(name = "token") String token);


    @PostMapping(value = "/feign/stock/save-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveDetail(@RequestBody List<DailyStockInfoDetailDTO> details, @RequestHeader(name = "token") String token);
}
