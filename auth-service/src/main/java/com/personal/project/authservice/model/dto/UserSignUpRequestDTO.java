package com.personal.project.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserSignUpRequestDTO {

	@NotNull(message = "username should not be null")
	@NotBlank(message = "username should not be empty")
	private String username;

	@NotBlank(message = "password should not be empty")
	@Length(max = 16, min = 8, message = "password length should between 8 to 16")
	private String password;
}
