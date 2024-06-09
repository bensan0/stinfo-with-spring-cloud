package com.personal.project.userservice.service.impl;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;
import com.personal.project.userservice.repository.dao.UserRegularCommentDao;
import com.personal.project.userservice.service.UserRegularCommentService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRegularCommentServiceImpl implements UserRegularCommentService {

    private final UserRegularCommentDao userRegularCommentDao;

    public UserRegularCommentServiceImpl(UserRegularCommentDao userRegularCommentDao) {
        this.userRegularCommentDao = userRegularCommentDao;
    }

    @Override
    public Optional<UserRegularCommentDO> findOne(Long userId) {
        return userRegularCommentDao.findOne(userId);
    }

    @Override
    public Optional<UserRegularCommentDO> findOneById(Long id) {
        return userRegularCommentDao.findOneById(id);
    }

    @Override
    public UserRegularCommentDO upsert(UserRegularCommentDO regularComment) {
        return userRegularCommentDao.upsert(regularComment);
    }
}
