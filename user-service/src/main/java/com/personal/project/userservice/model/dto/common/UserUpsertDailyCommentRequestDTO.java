package com.personal.project.userservice.model.dto.common;

import lombok.Data;

@Data
public class UserUpsertDailyCommentRequestDTO extends UserRequestDTO {

    private String username;

    private String password;
}
