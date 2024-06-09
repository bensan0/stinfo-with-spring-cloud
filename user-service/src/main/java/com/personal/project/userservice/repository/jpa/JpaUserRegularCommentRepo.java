package com.personal.project.userservice.repository.jpa;

import com.personal.project.userservice.model.entity.UserRegularCommentDO;
import com.personal.project.userservice.repository.UserRegularCommentRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRegularCommentRepo extends JpaRepository<UserRegularCommentDO, Long>, UserRegularCommentRepo {

}
