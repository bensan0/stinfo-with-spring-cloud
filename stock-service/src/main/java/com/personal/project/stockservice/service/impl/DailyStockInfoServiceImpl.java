package com.personal.project.stockservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockInfoMapper;
import com.personal.project.stockservice.model.dto.DailyStockInfoDTO;
import com.personal.project.stockservice.model.dto.Query4CalDTO;
import com.personal.project.stockservice.model.dto.StockInfo4InitDetailDTO;
import com.personal.project.stockservice.model.dto.StockInfo4InitMetricsDTO;
import com.personal.project.stockservice.model.entity.DailyStockInfoDO;
import com.personal.project.stockservice.service.DailyStockInfoService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyStockInfoServiceImpl extends ServiceImpl<DailyStockInfoMapper, DailyStockInfoDO> implements DailyStockInfoService {

    private final ApplicationContext applicationContext;
    private final DailyStockInfoMapper dailyStockInfoMapper;

    public DailyStockInfoServiceImpl(ApplicationContext applicationContext, DailyStockInfoMapper dailyStockInfoMapper) {
        this.applicationContext = applicationContext;
        this.dailyStockInfoMapper = dailyStockInfoMapper;
    }

    @Override
    public boolean initSaveAll(List<DailyStockInfoDTO> data) {
        List<DailyStockInfoDO> pojoList = data.stream()
                .map(dto -> BeanUtil.copyProperties(dto, DailyStockInfoDO.class))
                .sorted(Comparator.comparing(DailyStockInfoDO::getStockId))
                .sorted(Comparator.comparingLong(DailyStockInfoDO::getDate))
                .toList();

        return applicationContext.getBean(DailyStockInfoService.class).saveBatch(pojoList);
    }

    @Override
    public boolean saveAll(List<DailyStockInfoDTO> data) {
        List<DailyStockInfoDO> pojoList = data.stream().map(dto -> BeanUtil.copyProperties(dto, DailyStockInfoDO.class)).toList();

        return applicationContext.getBean(DailyStockInfoService.class).saveOrUpdateBatch(pojoList);
    }

    @Override
    public List<String> queryExist() {
        LambdaQueryWrapper<DailyStockInfoDO> wrapper  = new LambdaQueryWrapper();
        wrapper.select(DailyStockInfoDO::getStockId);
        wrapper.groupBy(DailyStockInfoDO::getStockId);
        List<DailyStockInfoDO> dailyStockInfoDOS = dailyStockInfoMapper.selectList(wrapper);

        return dailyStockInfoDOS.stream().map(DailyStockInfoDO::getStockId).toList();
    }

    @Override
    public Map<String, DailyStockInfoDTO> queryFormer(Long date) {
        List<DailyStockInfoDO> dailyStockInfoDOs = baseMapper.queryFormer(date);

        return dailyStockInfoDOs.stream()
                .map(d -> BeanUtil.copyProperties(d, DailyStockInfoDTO.class))
                .collect(Collectors.toMap(DailyStockInfoDTO::getStockId, Function.identity()));
    }

    @Override
    public Map<String, List<StockInfo4InitMetricsDTO>> query4InitYesterdayMetrics() {
        List<StockInfo4InitMetricsDTO> dtos = baseMapper.query4InitYesterdayMetrics();
        Map<String, List<StockInfo4InitMetricsDTO>> idToDTOs = new HashMap<>();
        dtos.forEach(dto -> {
            idToDTOs.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return idToDTOs;
    }

    @Override
    public Map<String, List<StockInfo4InitDetailDTO>> query4InitYesterdayDetail() {
        List<StockInfo4InitDetailDTO> dtos = baseMapper.queryInfo4InitYesterdayDetail();
        Map<String, List<StockInfo4InitDetailDTO>> idToDTOs = new HashMap<>();
        dtos.forEach(dto -> {
            idToDTOs.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return idToDTOs;
    }

    @Override
    public Map<String, List<StockInfo4InitMetricsDTO>> query4InitTodayMetrics() {
        List<StockInfo4InitMetricsDTO> dtos = baseMapper.query4InitTodayMetrics();
        Map<String, List<StockInfo4InitMetricsDTO>> idToDTOs = new HashMap<>();
        dtos.forEach(dto -> {
            idToDTOs.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return idToDTOs;
    }

    @Override
    public Map<String, List<DailyStockInfoDTO>> query4InitTodayDetail() {
        List<DailyStockInfoDTO> dtos = baseMapper.query4InitTodayDetail();
        Map<String, List<DailyStockInfoDTO>> idToDTOs = new HashMap<>();
        dtos.forEach(dto -> {
            idToDTOs.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return idToDTOs;
    }


    @Override
    public Map<String, List<DailyStockInfoDTO>> query4CalMetrics(Query4CalDTO query4CalDTO) {
        List<DailyStockInfoDTO> dailyStockInfoDTOS = baseMapper.query4CalMetrics(query4CalDTO);
        Map<String, List<DailyStockInfoDTO>> stockIdToInfos = new HashMap<>();
        dailyStockInfoDTOS.forEach(dto -> {
            stockIdToInfos.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return stockIdToInfos;
    }

    @Override
    public Map<String, List<DailyStockInfoDTO>> queryInfo4CalDetail(Query4CalDTO query4CalDto) {
        List<DailyStockInfoDTO> dtos = baseMapper.queryInfo4CalDetail(query4CalDto);
        Map<String, List<DailyStockInfoDTO>> stockIdToInfos = new HashMap<>();
        dtos.forEach(dto -> {
            stockIdToInfos.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return stockIdToInfos;
    }

    @Override
    public Map<String, DailyStockInfoDTO> queryByDate(long date) {
        LambdaQueryWrapper<DailyStockInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyStockInfoDO::getDate, date);
        List<DailyStockInfoDO> dos = dailyStockInfoMapper.selectList(wrapper);

        return dos.stream()
                .map(e -> BeanUtil.copyProperties(e, DailyStockInfoDTO.class))
                .collect(Collectors.toMap(DailyStockInfoDTO::getStockId, Function.identity()));
    }

    @Override
    public List<DailyStockInfoDTO> queryByDateAndId(Long date, String stockId) {
        LambdaQueryWrapper<DailyStockInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyStockInfoDO::getStockId, stockId)
                .isNotNull(DailyStockInfoDO::getTodayClosingPrice)
                .le(DailyStockInfoDO::getDate, date)
                .orderByDesc(DailyStockInfoDO::getDate)
                .last("limit 240");

        List<DailyStockInfoDO> dailyStockInfoDOs = dailyStockInfoMapper.selectList(wrapper);

        return dailyStockInfoDOs.stream()
                .map(e->BeanUtil.copyProperties(e, DailyStockInfoDTO.class))
                .toList();
    }
}
