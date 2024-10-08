package com.personal.project.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckTokenRequestDTO {

	@NotNull(message = "token should not be null")
	@NotBlank(message = "token should not be empty")
	private String token;
}
