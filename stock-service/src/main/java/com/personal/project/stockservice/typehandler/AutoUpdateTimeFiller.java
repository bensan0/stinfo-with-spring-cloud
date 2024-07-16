package com.personal.project.stockservice.typehandler;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AutoUpdateTimeFiller implements MetaObjectHandler {
	@Override
	public void insertFill(MetaObject metaObject) {
		this.setFieldValByName("updatedAt", Long.parseLong(LocalDateTime.now().format(DatePattern.PURE_DATETIME_FORMATTER)), metaObject);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		this.setFieldValByName("updatedAt", Long.parseLong(LocalDateTime.now().format(DatePattern.PURE_DATETIME_FORMATTER)), metaObject);
	}
}
