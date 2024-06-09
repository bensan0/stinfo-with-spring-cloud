package com.personal.project.userservice.repository.jpa;

import com.personal.project.userservice.model.entity.RoleDO;
import com.personal.project.userservice.repository.RoleRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(value = "repo", havingValue = "jpa", matchIfMissing = true)
@Repository
public interface JpaRoleRepo extends JpaRepository<RoleDO, Long>, RoleRepo {
}
