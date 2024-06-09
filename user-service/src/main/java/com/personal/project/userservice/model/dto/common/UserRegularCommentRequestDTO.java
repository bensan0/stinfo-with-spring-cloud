package com.personal.project.userservice.model.dto.common;

import lombok.Data;

@Data
public class UserRegularCommentRequestDTO extends UserRequestDTO {

    private Long id;

    private String comment;
}
