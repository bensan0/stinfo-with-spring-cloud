package com.personal.project.reportservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteStockService", value = ServiceNameConstants.STOCK_SERVICE, fallbackFactory = RemoteStockFallbackFactory.class)
public interface RemoteStockService {

    @PostMapping(value = "/feign/stock/query-4-cal-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(@RequestBody Query4CalMetricsDTO query4CalMetricsDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/manual/query-4-cal-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(@RequestBody Query4ManualCalDTO query4ManualCalDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/save-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveMetrics(@RequestBody List<DailyStockMetricsDTO> metrics, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/query-4-cal-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalDetailUnionDTO> getCalDetailInfo(@RequestBody Query4CalMetricsDTO query4CalDetailDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/manual/query-4-cal-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<CalDetailUnionDTO> getCalDetailInfo(@RequestBody Query4ManualCalDTO Query4ManualCalDTO, @RequestHeader(name = "token") String token);

    @PostMapping(value = "/feign/stock/save-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<ObjectUtils.Null> saveDetail(@RequestBody List<DailyStockInfoDetailDTO> details, @RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-4-init-yesterday-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<Map<String, List<StockInfo4InitMetricsDTO>>> get4CalInitYesterdayMetrics(@RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-4-init-yesterday-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> get4CalInitYesterdayDetail(@RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-4-init-today-metrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<Query4CalInitTodayMetricsDTO> get4CalInitTodayMetrics(@RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-4-init-today-detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<Query4CalInitTodayDetailDTO> get4CalInitTodayDetail(@RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-info-by-cond")
    InnerResponse<List<DailyStockInfoDTO>> getInfosByCond(@RequestParam Long date, @RequestParam String stockId);

    @GetMapping(value = "/feign/stock/query-4-cal-real-time-metrics")
    InnerResponse<CalMetricsUnionDTO> get4CalRealTimeMetrics(@RequestParam Long date, @RequestHeader(name = "token") String token);

    @GetMapping(value = "/feign/stock/query-4-cal-real-time-detail")
    InnerResponse<CalDetailUnionDTO> get4CalRealTimeDetailInfo(@RequestParam Long date, @RequestHeader(name = "token") String token);
}
