package com.personal.project.userservice.model.entity;

import lombok.Data;


@Data
public class UserRegularCommentDO {


	private Long id;


	private String comment;


	private UserDO userDO;


	private Long userId;

}
