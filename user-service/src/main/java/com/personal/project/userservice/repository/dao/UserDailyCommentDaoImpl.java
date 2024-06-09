package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.UserDailyCommentDO;
import com.personal.project.userservice.repository.UserDailyCommentRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class UserDailyCommentDaoImpl implements UserDailyCommentDao {

    private final UserDailyCommentRepo userDailyCommentRepo;

    public UserDailyCommentDaoImpl(@Qualifier("userDailyCommentRepo") UserDailyCommentRepo userDailyCommentRepo) {
        this.userDailyCommentRepo = userDailyCommentRepo;
    }

    @Override
    public int insert(Long userId, String comment, LocalDate created) {
        return userDailyCommentRepo.insert(userId, comment, created);
    }

    @Override
    public Optional<UserDailyCommentDO> findByUserIdAndCreated(Long userId, LocalDate date) {
        return userDailyCommentRepo.findByUserIdAndCreated(userId, date);
    }

    @Override
    public UserDailyCommentDO insert(UserDailyCommentDO userDailyCommentDO) {
        return userDailyCommentRepo.save(userDailyCommentDO);
    }
}
