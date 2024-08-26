package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.stockservice.model.dto.request.DailyIndexInfoDTO;
import com.personal.project.stockservice.model.entity.DailyIndexInfoDO;

import java.util.List;
import java.util.Map;

public interface DailyIndexInfoService extends IService<DailyIndexInfoDO> {

	boolean saveAll(List<DailyIndexInfoDTO> data);

	Map<String, DailyIndexInfoDTO> queryByDate(long date);

	List<DailyIndexInfoDTO> queryLatest();
}
