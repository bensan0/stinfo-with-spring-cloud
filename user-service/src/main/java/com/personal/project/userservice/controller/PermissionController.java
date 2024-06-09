package com.personal.project.userservice.controller;

import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.userservice.model.entity.PermissionDO;
import com.personal.project.userservice.service.PermissionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/add")
    public CommonResponse addPermission(
            @RequestBody PermissionDO permissionDO) {
        permissionService.add(permissionDO);

        return CommonResponse.ok(null);
    }
}
