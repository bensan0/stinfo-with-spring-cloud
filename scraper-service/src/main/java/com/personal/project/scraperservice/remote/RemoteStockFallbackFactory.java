package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

public class RemoteStockFallbackFactory implements FallbackFactory<RemoteStockService> {
    @Override
    public RemoteStockService create(Throwable cause) {
        return new RemoteStockService() {
            @Override
            public InnerResponse<Map<String, DailyStockInfoDto>> getByDate(Long date, String token) {
                return null;
            }

            @Override
            public InnerResponse<ObjectUtils.Null> saveAll(List<DailyStockInfoDto> data, String token) {

                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> initSaveAll(List<DailyStockInfoDto> data, String token) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Map<String, DailyStockInfoDto>> getFormer(Long date, String token) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<Map<String, String>> getExist() {
                return getBasicFailedResp(cause);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    private <T> InnerResponse<T> getBasicFailedResp(Throwable cause){

        return InnerResponse.failed(ResponseCode.Failed.getCode(), "some thing go wrong: " + cause.getMessage());
    }
}
