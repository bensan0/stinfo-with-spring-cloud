package com.personal.project.userservice.model.dto.common;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDailyCommentRequestDTO {

	private Long id;

	private String comment;

	private LocalDate created = LocalDate.now();
}
