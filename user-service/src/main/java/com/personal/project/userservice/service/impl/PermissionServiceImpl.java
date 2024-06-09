package com.personal.project.userservice.service.impl;

import com.personal.project.userservice.model.entity.PermissionDO;
import com.personal.project.userservice.repository.PermissionRepo;
import com.personal.project.userservice.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepo permissionRepo;

    public PermissionServiceImpl(PermissionRepo permissionRepo){
        this.permissionRepo = permissionRepo;
    }

    @Override
    public void add(PermissionDO permissionDO) {
        permissionRepo.save(permissionDO);
    }
}
