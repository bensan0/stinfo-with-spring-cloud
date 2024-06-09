package com.personal.project.userservice.repository.jpa;

import com.personal.project.userservice.model.entity.User;
import com.personal.project.userservice.repository.UserRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Qualifier("userRepo")
@ConditionalOnProperty(value = "repo", havingValue = "jpa", matchIfMissing = true)
@Repository
public interface JpaUserRepo extends JpaRepository<User, Long>, UserRepo {
}
