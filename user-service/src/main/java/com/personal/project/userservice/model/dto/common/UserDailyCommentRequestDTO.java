package com.personal.project.userservice.model.dto.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDailyCommentRequestDTO extends UserRequestDTO {

    private Long id;

    private String comment;

    private LocalDate created = LocalDate.now();
}
