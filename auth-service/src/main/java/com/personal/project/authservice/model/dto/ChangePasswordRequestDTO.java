package com.personal.project.authservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ChangePasswordRequestDTO {

	@NotNull(message = "密碼不可為空值")
	@NotBlank(message = "密碼不可為空字串")
	private String nowPassword;

	@NotNull(message = "新密碼不可為空值")
	@NotBlank(message = "新密碼不可為空字串")
	@Length(min = 6, message = "新密碼長度不可低於6")
	private String newPassword;

	@NotNull(message = "用戶名不可為空值")
	@NotBlank(message = "用戶名不可為空字串")
	private String username;
}
