package com.personal.project.reportservice.remote;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
        };
    }
}
