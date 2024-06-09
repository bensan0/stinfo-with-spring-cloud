package com.personal.project.userservice.repository;

import com.personal.project.userservice.model.entity.RoleDO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RoleRepo extends CrudRepository<RoleDO, Long>, PagingAndSortingRepository<RoleDO, Long>{

    RoleDO findByName(String name);
}
