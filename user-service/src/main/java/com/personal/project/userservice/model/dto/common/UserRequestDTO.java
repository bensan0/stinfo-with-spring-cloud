package com.personal.project.userservice.model.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "user id should not be blank")
    private Long userId;
}
