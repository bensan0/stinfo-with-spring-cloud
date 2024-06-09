package com.personal.project.userservice.repository;

import com.personal.project.userservice.model.entity.PermissionDO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepo extends PagingAndSortingRepository<PermissionDO, Long>, CrudRepository<PermissionDO, Long> {
}
