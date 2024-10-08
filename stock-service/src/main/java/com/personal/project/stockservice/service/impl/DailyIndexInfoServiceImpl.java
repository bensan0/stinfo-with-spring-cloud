package com.personal.project.stockservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyIndexInfoMapper;
import com.personal.project.stockservice.model.dto.request.DailyIndexInfoDTO;
import com.personal.project.stockservice.model.entity.DailyIndexInfoDO;
import com.personal.project.stockservice.service.DailyIndexInfoService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class DailyIndexInfoServiceImpl extends ServiceImpl<DailyIndexInfoMapper, DailyIndexInfoDO> implements DailyIndexInfoService {

	private final ApplicationContext applicationContext;
	private final DailyIndexInfoMapper dailyIndexInfoMapper;

	@Override
	public boolean saveAll(List<DailyIndexInfoDTO> data) {
		List<DailyIndexInfoDO> pojoList = data.stream().map(dto -> BeanUtil.copyProperties(dto, DailyIndexInfoDO.class)).toList();

		return applicationContext.getBean(DailyIndexInfoService.class).saveOrUpdateBatch(pojoList);
	}

	@Override
	public Map<String, DailyIndexInfoDTO> queryByDate(long date) {
		LambdaQueryWrapper<DailyIndexInfoDO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DailyIndexInfoDO::getDate, date);
		List<DailyIndexInfoDO> dos = dailyIndexInfoMapper.selectList(wrapper);

		return dos.stream()
				.map(e -> BeanUtil.copyProperties(e, DailyIndexInfoDTO.class))
				.collect(Collectors.toMap(DailyIndexInfoDTO::getIndexName, Function.identity()));
	}

	@Override
	public List<DailyIndexInfoDTO> queryLatest() {
		List<DailyIndexInfoDO> dos = dailyIndexInfoMapper.queryLatest();

		return dos.stream()
				.map(e -> BeanUtil.copyProperties(e, DailyIndexInfoDTO.class))
				.toList();
	}
}
