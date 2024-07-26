package com.personal.project.gateway.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("connection_info")
public class ConnectionInfoDO {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String ip;

	private String path;

	private String date;
}
