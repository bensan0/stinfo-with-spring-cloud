package com.personal.project.scraperservice.remote;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteReportFallbackFactory  implements FallbackFactory<RemoteReportService> {

    @Override
    public RemoteReportService create(Throwable cause) {

        return new RemoteReportService() {
            @Override
            public InnerResponse<ObjectUtils.Null> initYesterdayReport(String token) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> initTodayReport(String token) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> genRealTimeMetricsReport(Long date, String token) {
                return getBasicFailedResp(cause);
            }

            @Override
            public InnerResponse<ObjectUtils.Null> genRealTimeDetailReport(Long date, String token) {
                return getBasicFailedResp(cause);
            }
        };
    }

    private <T> InnerResponse<T> getBasicFailedResp(Throwable cause){

        return InnerResponse.failed(ResponseCode.Failed.getCode(), "some thing go wrong: " + cause.getMessage());
    }
}
