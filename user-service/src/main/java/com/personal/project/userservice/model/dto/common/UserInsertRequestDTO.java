package com.personal.project.userservice.model.dto.common;

import com.personal.project.userservice.model.entity.RoleDO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserInsertRequestDTO extends UserRequestDTO {

    @NotBlank(message = "username should not be empty")
    private String username;

    @NotBlank(message = "password should not be empty")
    @Length(max = 32, min = 16, message = "password length should between 6 to 32")
    private String password;

    @NotNull(message = "role should not be null")
    private RoleDO roleDO;
}
