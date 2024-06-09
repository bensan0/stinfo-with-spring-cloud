package com.personal.project.userservice.service.impl;

import com.alibaba.fastjson2.JSON;
import com.personal.project.userservice.model.entity.RoleDO;
import com.personal.project.userservice.repository.RoleRepo;
import com.personal.project.userservice.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    public RoleServiceImpl(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public void add(RoleDO roleDO) {
        roleRepo.save(roleDO);
    }

    @Override
    public RoleDO findOne(String name) {
        return roleRepo.findByName(name);
    }

    @Override
    public RoleDO update(RoleDO roleDO) {
        if (roleDO.getId() == null) {
            log.error("Update role.id cannot be null, role: {}", JSON.toJSONString(roleDO));
            return null;
        }

        return roleRepo.save(roleDO);
    }


}
