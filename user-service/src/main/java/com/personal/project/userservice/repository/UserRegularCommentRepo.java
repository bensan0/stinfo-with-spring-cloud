package com.personal.project.userservice.repository;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;

import java.util.Optional;

public interface UserRegularCommentRepo {

    Optional<UserRegularCommentDO> findFirstByUserId(Long userId);

    Optional<UserRegularCommentDO> findFirstById(Long id);

    <S extends UserRegularCommentDO> S save(S entity);
}
