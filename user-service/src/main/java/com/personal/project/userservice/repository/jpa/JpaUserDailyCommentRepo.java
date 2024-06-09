package com.personal.project.userservice.repository.jpa;

import com.personal.project.userservice.model.entity.UserDailyCommentDO;
import com.personal.project.userservice.repository.UserDailyCommentRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Qualifier("userDailyCommentRepo")
@ConditionalOnProperty(value = "repo", havingValue = "jpa", matchIfMissing = true)
@Repository
public interface JpaUserDailyCommentRepo extends JpaRepository<UserDailyCommentDO, Long>, UserDailyCommentRepo{

    @Query(value = "select c from UserDailyCommentDO c where c.userId = :userId and c.created = :date")
    Optional<UserDailyCommentDO> findByUserIdAndCreated(@Param("userId") Long userId,
                                                        @Param("date") LocalDate date);

    @Override
    @Modifying
    @Query(value = "insert into user_daily_comment (userId, comment, created) values (:userId, :comment, :created)", nativeQuery = true)
    int insert(@Param("userId") Long userId,
               @Param("comment") String comment,
               @Param("created") LocalDate created);

}
