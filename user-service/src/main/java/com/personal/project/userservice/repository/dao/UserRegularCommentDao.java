package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;

import java.util.Optional;

public interface UserRegularCommentDao {

    Optional<UserRegularCommentDO> findOne(Long userId);

    Optional<UserRegularCommentDO> findOneById(Long id);

    UserRegularCommentDO upsert(UserRegularCommentDO regularComment);
}
