package com.personal.project.userservice.model.dto.common;

public record UserDailyCommentDTO(
        Long id,
        Long userId,
        String comment) {
}
