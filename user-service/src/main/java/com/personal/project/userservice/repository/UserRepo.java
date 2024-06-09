package com.personal.project.userservice.repository;

import com.personal.project.userservice.model.entity.User;

import java.util.Optional;

public interface UserRepo{

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    <S extends User> S save(S entity);
}
