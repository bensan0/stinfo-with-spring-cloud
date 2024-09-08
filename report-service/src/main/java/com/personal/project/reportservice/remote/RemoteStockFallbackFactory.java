package com.personal.project.reportservice.remote;

import com.personal.project.commoncore.constants.ResponseCode;
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
            @Override
            public InnerResponse<CalMetricsUnionDTO> getCalMetricsInfo(Query4CalMetricsDTO query4CalMetricsDto) {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> saveMetrics(List<DailyStockMetricsDTO> metrics) {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<CalDetailUnionDTO> getCalDetailInfo(Query4CalMetricsDTO query4CalMetricsDto) {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> saveDetail(List<DailyStockInfoDetailDTO> details) {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Map<String, List<StockInfo4InitMetricsDTO>>> get4CalInitYesterdayMetrics() {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> get4CalInitYesterdayDetail() {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Query4CalInitTodayMetricsDTO> get4CalInitTodayMetrics() {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Query4CalInitTodayDetailDTO> get4CalInitTodayDetail() {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<List<DailyStockInfoDTO>> getInfosByCond(Long date, String stockId) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<CalMetricsUnionDTO> get4CalRealTimeMetrics(Long date) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<CalDetailUnionDTO> get4CalRealTimeDetailInfo(Long date) {
                return getBasicFailedResp(cause);
            }
        };
    }

    private <T> InnerResponse<T> getBasicFailedResp(Throwable cause){

        return InnerResponse.failed(ResponseCode.Failed.getCode(), "some thing go wrong: " + cause.getMessage());
    }
}
