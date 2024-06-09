package com.personal.project.userservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "plat_permission")
@Getter
@Setter
public class PermissionDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 功能
     */
    @Column(unique = true, nullable = false)
    private String feature;

    /**
     * 已刪除 0=未刪, 1=已刪
     */
    @Column(name = "deleted", nullable = false, length = 1)
    private int deleted = 0;

    /**
     * 第一階層顯示順位
     */
    @Column(name = "class_1_idx", nullable = false)
    private Integer class1;

    /**
     * 第二階層顯示順位
     */
    @Column(name = "class_2_idx", nullable = false)
    private Integer class2;

    /**
     * 第三階層顯示順位
     */
    @Column(name = "class_3_idx", nullable = false)
    private Integer class3 = 0;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    private Set<RoleDO> roleDOs;
}
