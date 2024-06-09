package com.personal.project.userservice.repository.dao;

import com.personal.project.userservice.model.entity.User;
import com.personal.project.userservice.repository.UserRepo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDaoImpl implements UserDao {

    private final UserRepo userRepo;

    public UserDaoImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Optional<User> findOne(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public Optional<User> findOne(Long id) {
        return userRepo.findById(id);
    }

    @Override
    public User upsert(User user) {
        return userRepo.save(user);
    }
}
