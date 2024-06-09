package com.personal.project.userservice.service;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;

import java.util.Optional;

public interface UserRegularCommentService {

    Optional<UserRegularCommentDO> findOne(Long userId);

    Optional<UserRegularCommentDO> findOneById(Long id);

    UserRegularCommentDO upsert(UserRegularCommentDO regularComment);

}
