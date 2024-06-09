package com.personal.project.userservice.service.impl;

import com.personal.project.userservice.exceptionhandler.exception.WrongArgsException;
import com.personal.project.userservice.model.entity.UserDailyCommentDO;
import com.personal.project.userservice.repository.UserDailyCommentRepo;
import com.personal.project.userservice.service.UserDailyCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class UserDailyCommentServiceImpl implements UserDailyCommentService {

    private final UserDailyCommentRepo userDailyCommentRepo;

    public UserDailyCommentServiceImpl(UserDailyCommentRepo userDailyCommentRepo) {
        this.userDailyCommentRepo = userDailyCommentRepo;
    }

    @Override
    public Optional<UserDailyCommentDO> findOne(Long userId, LocalDate created) {

        if (userId == null || created == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("userId=").append(userId).append(";")
                    .append("created=").append(created);
            throw new WrongArgsException("User id or created should not be null", sb.toString());
        }

        return userDailyCommentRepo.findByUserIdAndCreated(userId, created);
    }

    @Override
    public UserDailyCommentDO insert(UserDailyCommentDO userDailyCommentDO) {
        if (userDailyCommentDO == null) {
            throw new WrongArgsException("UserDailyComment should not be null");
        }

        return userDailyCommentRepo.save(userDailyCommentDO);
    }

    @Override
    public boolean insert(Long userId, String comment, LocalDate created) {
        int result = userDailyCommentRepo.insert(userId, comment, created);

        return 1 == result;
    }
}
