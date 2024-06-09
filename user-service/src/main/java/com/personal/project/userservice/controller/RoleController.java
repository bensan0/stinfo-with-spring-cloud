package com.personal.project.userservice.controller;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.userservice.model.entity.RoleDO;
import com.personal.project.userservice.service.RoleService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/role")
@Slf4j
@Validated
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/{roleName}/info")
    public CommonResponse getRole(@PathVariable("roleName") @NotBlank(message = "role name cannot be null or blank") String roleName) {
        RoleDO roleDO = roleService.findOne(roleName);

        return roleDO != null ? CommonResponse.ok(roleDO) : CommonResponse.error(ResponseCode.Not_Found, null);
    }


    @PostMapping("/add")
    public CommonResponse add(@RequestBody RoleDO roleDO) {

        roleService.add(roleDO);
        return CommonResponse.ok(null);
    }
}
