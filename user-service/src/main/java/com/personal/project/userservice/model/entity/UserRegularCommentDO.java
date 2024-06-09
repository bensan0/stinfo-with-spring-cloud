package com.personal.project.userservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_regular_comment")
@Data
public class UserRegularCommentDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "mediumtext")
    private String comment;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    public static UserRegularCommentDO of(String comment, Long userId){
        UserRegularCommentDO regularComment = new UserRegularCommentDO();
        regularComment.setComment(comment);
        regularComment.setUserId(userId);

        return regularComment;
    }
}
