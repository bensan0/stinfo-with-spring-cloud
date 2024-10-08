package com.personal.project.userservice.model.entity;

import lombok.Data;

import java.time.LocalDate;


@Data
public class UserDailyCommentDO {


	private Long id;


	private String comment;


	private LocalDate created = LocalDate.now();


	private Long userId;
}
