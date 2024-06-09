package com.personal.project.stockservice.remote;

import com.personal.project.commoncore.constants.ServiceNameConstants;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.stockservice.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.USER_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService {

    @GetMapping(value = "/feign/user/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    InnerResponse<UserDto> getOne(@PathVariable("userId") Integer userId, @RequestHeader(name = "token") String token);
}
