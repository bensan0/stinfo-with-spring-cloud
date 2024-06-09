package com.personal.project.userservice.model.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.personal.project.userservice.model.entity.RoleDO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDTO(Long id, String username, String password, RoleDO roleDO) {
}
