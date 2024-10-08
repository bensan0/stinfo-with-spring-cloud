package com.personal.project.authservice.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

	Reviewing(0),
	Enable(1),
	Disable(2),
	;

	@EnumValue
	private final Integer status;
}
