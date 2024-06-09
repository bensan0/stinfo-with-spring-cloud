package com.personal.project.userservice.model.dto.common;

import com.personal.project.userservice.model.entity.RoleDO;
import lombok.Data;

@Data
public class UserUpdateRequestDTO extends UserRequestDTO {

    private RoleDO roleDO;
}
