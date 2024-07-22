package com.personal.project.authservice.model.dto;

import lombok.Data;

@Data
public class UserDTO {

	private Long id;

	private String username;

	private Long createdAt;

	private Long updatedAt;

	private Integer status;
}
