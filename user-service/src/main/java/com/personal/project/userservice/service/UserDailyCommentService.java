package com.personal.project.userservice.service;

import com.personal.project.userservice.model.entity.UserDailyCommentDO;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyCommentService {

    Optional<UserDailyCommentDO> findOne(Long userId, LocalDate created);

    UserDailyCommentDO insert(UserDailyCommentDO userDailyCommentDO);

    boolean insert(Long userId, String comment, LocalDate created);
}
