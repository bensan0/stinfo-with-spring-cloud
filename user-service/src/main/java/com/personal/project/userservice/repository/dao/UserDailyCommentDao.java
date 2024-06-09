package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.UserDailyCommentDO;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyCommentDao {

    int insert(Long userId, String comment, LocalDate created);

    Optional<UserDailyCommentDO> findByUserIdAndCreated(Long userId, LocalDate date);

    UserDailyCommentDO insert(UserDailyCommentDO userDailyCommentDO);
}
