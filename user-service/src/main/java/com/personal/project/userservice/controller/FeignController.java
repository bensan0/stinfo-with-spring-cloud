package com.personal.project.userservice.controller;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.userservice.model.dto.feign.UserDTO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/user")
public class FeignController {

    //todo 回傳改為Dto ex: InnerResponse<T> {"code":"", "message":"", "data": T}
    @GetMapping("/{userId}")
    public InnerResponse<UserDTO> getOne(@PathVariable("userId")
                                         @NotBlank(message = "user id cannot be null or blank")
                                         String userId) {
        //todo 實現邏輯
        return null;
    }
}
