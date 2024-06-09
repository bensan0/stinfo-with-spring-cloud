package com.personal.project.userservice.service;

import com.personal.project.userservice.model.entity.RoleDO;

public interface RoleService {

    void add(RoleDO roleDO);

    RoleDO findOne(String name);

    RoleDO update(RoleDO roleDO);
}
