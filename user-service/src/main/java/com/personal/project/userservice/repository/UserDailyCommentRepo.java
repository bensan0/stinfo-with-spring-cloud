package com.personal.project.userservice.repository;

import com.personal.project.userservice.model.entity.UserDailyCommentDO;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyCommentRepo {

    Optional<UserDailyCommentDO> findByUserIdAndCreated(Long userId, LocalDate date);

    int insert(Long userId, String comment, LocalDate created);

    <S extends UserDailyCommentDO> S save(S entity);
}
