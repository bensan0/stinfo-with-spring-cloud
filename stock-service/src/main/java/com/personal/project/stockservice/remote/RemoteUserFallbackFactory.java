package com.personal.project.stockservice.remote;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.UserDto;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService> {

    @Override
    public RemoteUserService create(Throwable cause) {
        return new RemoteUserService() {
            //todo 實現回傳Dto ex: InnerResponse<T> {"code":"", "message":"", "data": T}
            @Override
            public InnerResponse<UserDto> getOne(Integer userId, String token) {
                //todo 實現熔斷回傳
                return null;
            }
        };
    }
}
