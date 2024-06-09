package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;
import com.personal.project.userservice.repository.UserRegularCommentRepo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRegularCommentDaoImpl implements UserRegularCommentDao {

    private final UserRegularCommentRepo userRegularCommentRepo;

    public UserRegularCommentDaoImpl(UserRegularCommentRepo userRegularCommentRepo) {
        this.userRegularCommentRepo = userRegularCommentRepo;
    }

    @Override
    public Optional<UserRegularCommentDO> findOne(Long userId) {
        return userRegularCommentRepo.findFirstByUserId(userId);
    }

    @Override
    public Optional<UserRegularCommentDO> findOneById(Long id) {
        return userRegularCommentRepo.findFirstById(id);
    }

    @Override
    public UserRegularCommentDO upsert(UserRegularCommentDO regularComment) {
        return userRegularCommentRepo.save(regularComment);
    }
}
