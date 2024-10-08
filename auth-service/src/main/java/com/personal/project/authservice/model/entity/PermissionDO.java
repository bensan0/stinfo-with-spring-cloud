package com.personal.project.authservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("plat_permission")
public class PermissionDO {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String resource;
}
