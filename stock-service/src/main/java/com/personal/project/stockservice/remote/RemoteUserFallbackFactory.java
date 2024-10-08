package com.personal.project.stockservice.remote;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.UserDto;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService> {

    @Override
    public RemoteUserService create(Throwable cause) {
        return new RemoteUserService() {
            @Override
            public InnerResponse<UserDto> getOne(Integer userId, String token) {

                return getBasicFailedResp(cause);
            }
        };
    }

    private <T> InnerResponse<T> getBasicFailedResp(Throwable cause){

        return InnerResponse.failed(ResponseCode.Failed.getCode(), "some thing go wrong: " + cause.getMessage());
    }
}
