package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> findOne(String username);

    Optional<User> findOne(Long id);

    User upsert(User user);
}
