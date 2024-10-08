package com.personal.project.authservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.personal.project.authservice.constant.UserStatus;
import lombok.Data;

@Data
@TableName("plat_user")
public class UserDO {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String username;

	private String password;

	private String salt;

	private Integer deleted = 0;

	@TableField(fill = FieldFill.INSERT)
	private Long createdAt;

	@TableField(fill = FieldFill.UPDATE)
	private Long updatedAt;

	private UserStatus status = UserStatus.Reviewing;

//	private Long roleId;

//	@TableField(exist = false)
//	private RoleDO role;
}
