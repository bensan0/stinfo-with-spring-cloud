package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personal.project.stockservice.model.entity.DailyIndexInfoDO;

import java.util.List;

public interface DailyIndexInfoMapper extends BaseMapper<DailyIndexInfoDO> {
	List<DailyIndexInfoDO> queryLatest();
}
