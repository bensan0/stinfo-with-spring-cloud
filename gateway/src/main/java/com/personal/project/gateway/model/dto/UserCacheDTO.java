package com.personal.project.gateway.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserCacheDTO {

	private Long id;

	private String username;

	private Long createdAt;

	private Long updatedAt;

	private Integer status;

	List<String> permissions;
}
