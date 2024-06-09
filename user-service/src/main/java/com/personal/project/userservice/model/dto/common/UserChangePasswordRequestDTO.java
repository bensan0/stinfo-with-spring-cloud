package com.personal.project.userservice.model.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserChangePasswordRequestDTO extends UserRequestDTO {

    @NotBlank(message = "former password can not be empty")
    private String formerPassword;

    @NotBlank(message = "new password can not be empty")
    @Length(max = 32, min = 6, message = "new password length should between 6 to 32")
    private String newPassword;
}
