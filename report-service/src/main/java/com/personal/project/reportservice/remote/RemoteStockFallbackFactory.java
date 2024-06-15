package com.personal.project.reportservice.remote;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

public class RemoteStockFallbackFactory implements FallbackFactory<RemoteStockService> {
    @Override
    public RemoteStockService create(Throwable cause) {
        return new RemoteStockService() {
            //todo 實現回傳Dto ex: InnerResponse<T> {"code":"", "message":"", "data": T}
            @Override
            public InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(Query4CalMetricsDTO query4CalMetricsDto, String token) {
                //todo 實現熔斷回傳
                return null;
            }

            @Override
            public InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(Query4ManualCalDTO query4ManualCalDTO, String token) {
                return null;
            }

            @Override
            public InnerResponse<ObjectUtils.Null> saveMetrics(List<DailyStockMetricsDTO> metrics, String token) {
                //todo 實現熔斷回傳
                return null;
            }

            @Override
            public InnerResponse<CalDetailUnionDTO> getCalDetailInfo(Query4CalMetricsDTO query4CalMetricsDto, String token) {
                //todo 實現熔斷回傳
                return null;
            }

            @Override
            public InnerResponse<CalDetailUnionDTO> getCalDetailInfo(Query4ManualCalDTO Query4ManualCalDTO, String token) {
                return null;
            }

            @Override
            public InnerResponse<ObjectUtils.Null> saveDetail(List<DailyStockInfoDetailDTO> details, String token) {
                //todo 實現熔斷回傳
                return null;
            }

            @Override
            public InnerResponse<Map<String, List<StockInfo4InitMetricsDTO>>> get4CalInitYesterdayMetrics(String token) {
                return null;
            }

            @Override
            public InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> get4CalInitYesterdayDetail(String token) {
                return null;
            }

            @Override
            public InnerResponse<Query4CalInitTodayMetricsDTO> get4CalInitTodayMetrics(String token) {
                return null;
            }

            @Override
            public InnerResponse<Query4CalInitTodayDetailDTO> get4CalInitTodayDetail(String token) {
                return null;
            }
        };
    }
}
