package com.personal.project.userservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plat_user")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, updatable = false, length = 32)
    private String username;

    @Column(name = "password", nullable = false, length = 32)
    private String password;

    @Column(name = "salt", nullable = false, length = 16)
    private String salt;

    @Column(name = "deleted", nullable = false, length = 1)
    private Integer deleted = 0;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleDO roleDO;

    @Column(name = "role_id", insertable = false, updatable = false)
    private Long roleId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<UserDailyCommentDO> userDailyCommentDOS;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private UserRegularCommentDO userRegularCommentDO;
}
