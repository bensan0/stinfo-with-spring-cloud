package com.personal.project.authservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.List;

@TableName("plat_role")
public class RoleDO {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	@TableField(exist = false)
	private List<PermissionDO> permissions;
}
