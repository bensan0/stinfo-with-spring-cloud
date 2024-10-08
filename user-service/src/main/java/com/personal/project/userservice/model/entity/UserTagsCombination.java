package com.personal.project.userservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_tags_combination")
//常用標籤組合
public class UserTagsCombination {

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long userId;

	private String tagsCombination;
}
